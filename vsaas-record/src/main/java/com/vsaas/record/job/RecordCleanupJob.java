package com.vsaas.record.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vsaas.record.entity.RecordFile;
import com.vsaas.record.entity.RecordPlan;
import com.vsaas.record.mapper.RecordFileMapper;
import com.vsaas.record.mapper.RecordPlanMapper;
import com.vsaas.record.service.RecordStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordCleanupJob {

    private final RecordPlanMapper planMapper;
    private final RecordFileMapper fileMapper;
    private final RecordStorageService storageService;

    @Scheduled(cron = "${vsaas.record.cleanup-cron:0 0 4 * * ?}")
    @SchedulerLock(name = "cleanup", lockAtMostFor = "PT2H", lockAtLeastFor = "PT30M")
    public void cleanup() {
        List<RecordPlan> plans = planMapper.selectList(
                new LambdaQueryWrapper<RecordPlan>().eq(RecordPlan::getStatus, 1));
        Map<Long, Integer> retainMap = new HashMap<>();
        for (RecordPlan p : plans) {
            int days = p.getRetainDays() != null ? p.getRetainDays() : 7;
            retainMap.merge(p.getChannelId(), days, Math::min);
        }
        int deleted = 0;
        for (Map.Entry<Long, Integer> e : retainMap.entrySet()) {
            LocalDateTime expireBefore = LocalDateTime.now().minusDays(e.getValue());
            List<RecordFile> oldFiles = fileMapper.selectList(
                    new LambdaQueryWrapper<RecordFile>()
                            .eq(RecordFile::getChannelId, e.getKey())
                            .lt(RecordFile::getEndTime, expireBefore)
                            .last("LIMIT 500"));
            for (RecordFile f : oldFiles) {
                try { storageService.deleteObject(f.getFilePath()); } catch (Exception ignored) {}
                fileMapper.deleteById(f.getId());
                deleted++;
            }
        }
        LocalDateTime defaultExpire = LocalDateTime.now().minusDays(7);
        List<RecordFile> orphans = fileMapper.selectList(
                new LambdaQueryWrapper<RecordFile>()
                        .lt(RecordFile::getEndTime, defaultExpire)
                        .last("LIMIT 200"));
        for (RecordFile f : orphans) {
            if (!retainMap.containsKey(f.getChannelId())) {
                try { storageService.deleteObject(f.getFilePath()); } catch (Exception ignored) {}
                fileMapper.deleteById(f.getId());
                deleted++;
            }
        }
        log.info("录像过期清理完成 deleted={}", deleted);
    }
}
