<template>
  <div>
    <h2>告警中心</h2>
    <el-table :data="list" border stripe style="margin-top:16px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="alarmType" label="类型" width="120" />
      <el-table-column prop="alarmContent" label="内容" />
      <el-table-column prop="alarmTime" label="时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已处理' : '未处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link @click="onHandle(row.id)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlarmList, handleAlarm } from '../api'
const list = ref([])
const loading = ref(false)
async function load() {
  loading.value = true
  try {
    const res = await getAlarmList({ page: 1, size: 50 })
    list.value = res.data?.records || []
  } finally { loading.value = false }
}
async function onHandle(id) {
  await handleAlarm(id)
  ElMessage.success('已处理')
  load()
}
onMounted(load)
</script>
