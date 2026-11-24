# VSaaS 完整业务闭环说明

默认租户 `tenantId=1`，演示账号 `admin/admin123`。

## 一、直播闭环

1. 登录获取 Token  
2. 演示设备 `demo-cam-001`（id=100）与通道 `1001/1002`  
3. 预览：
```bash
curl "http://127.0.0.1:8003/api/media/play/channel/1001" -H "X-Tenant-Id: 1" -H "X-User-Id: 1"
ffmpeg -re -i your.mp4 -c copy -f flv rtmp://127.0.0.1/live/1_1001
```

## 二、录像闭环

SRS `on_dvr` → record 入库；或手动 `POST /api/record/file/report`。  
前端「录像回放」按通道+时段查询。

## 三、告警闭环

```bash
curl -X POST http://127.0.0.1:8006/api/alarm/report \
  -H "Content-Type: application/json" -H "X-Tenant-Id: 1" -H "X-User-Id: 1" \
  -d '{"channelId":1001,"alarmType":"motion","alarmContent":"移动侦测"}'
# WS: ws://127.0.0.1:8006/ws/alarm?tenantId=1
```

## 四、计费闭环

```bash
curl -X POST http://127.0.0.1:8007/api/billing/usage/report \
  -H "Content-Type: application/json" -H "X-Tenant-Id: 1" -H "X-User-Id: 1" \
  -d '{"usageType":"channel","usageValue":2}'
curl -X POST "http://127.0.0.1:8007/api/billing/bill/generate?billMonth=2026-11" \
  -H "X-Tenant-Id: 1" -H "X-User-Id: 1"
curl -X POST http://127.0.0.1:8007/api/billing/pay/create/{billId} -H "X-Tenant-Id: 1"
curl -X POST "http://127.0.0.1:8007/api/billing/pay/mock/confirm?billId={billId}" -H "X-Tenant-Id: 1"
```

## 五、生产级硬闭环

| 链路 | 机制 |
|------|------|
| 录制入库 | SRS on_dvr → record |
| 离线告警 | DeviceOfflineCheckJob → alarm |
| 月结 | MonthlyBillJob + ShedLock |
| 配额/欠费 | QuotaService / ArrearsService |
| 播放防盗链 | PlayToken HMAC |
| Hook 鉴权 | X-Hook-Secret |

## 六、启动

```bash
bash scripts/start-dev.sh
# 后端各模块 spring-boot:run
# cd frontend && npm i && npm run dev
```

详见 `docs/` 与 `README.md`。
