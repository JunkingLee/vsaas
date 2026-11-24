<template>
  <div>
    <h2>仪表盘</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">设备总数</div>
          <div class="num">{{ stats.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">在线设备</div>
          <div class="num" style="color:#67C23A">{{ stats.online || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">离线设备</div>
          <div class="num" style="color:#F56C6C">{{ stats.offline || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOnlineStats } from '../api'
const stats = ref({})
onMounted(async () => {
  try {
    const res = await getOnlineStats()
    stats.value = res.data || {}
  } catch (e) {}
})
</script>

<style scoped>
.stat { color: #909399; font-size: 14px; }
.num { font-size: 28px; font-weight: bold; margin-top: 8px; }
</style>
