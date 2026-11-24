<template>
  <div>
    <h2>录像回放</h2>
    <el-form inline style="margin: 16px 0">
      <el-form-item label="通道ID">
        <el-input v-model="channelId" style="width:120px" placeholder="1001" />
      </el-form-item>
      <el-form-item label="时段">
        <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadFiles">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="files" border stripe v-loading="loading">
      <el-table-column prop="startTime" label="开始" />
      <el-table-column prop="endTime" label="结束" />
      <el-table-column prop="fileSize" label="大小" width="100" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" link @click="playFile(row)">播放</el-button>
        </template>
      </el-table-column>
    </el-table>
    <video ref="videoRef" controls style="width:100%;max-height:420px;background:#000;margin-top:16px"></video>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const channelId = ref('')
const range = ref([])
const files = ref([])
const loading = ref(false)
const videoRef = ref(null)
async function loadFiles() {
  if (!channelId.value || !range.value || range.value.length !== 2) {
    ElMessage.warning('请填写通道和时段'); return
  }
  loading.value = true
  try {
    const res = await request.get('http://127.0.0.1:8005/api/record/file/list', {
      params: { channelId: channelId.value, startTime: range.value[0], endTime: range.value[1], page: 1, size: 200 }
    })
    files.value = res.data?.records || []
  } finally { loading.value = false }
}
async function playFile(row) {
  try {
    const res = await request.get('http://127.0.0.1:8005/api/record/file/' + row.id + '/play')
    const url = res.data?.playUrl || row.filePath
    if (videoRef.value) { videoRef.value.src = url; videoRef.value.play() }
  } catch (e) {
    if (videoRef.value && row.filePath) videoRef.value.src = row.filePath
  }
}
</script>
