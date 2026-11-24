<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <h2>设备管理</h2>
      <el-button type="primary" @click="showAdd = true">新增设备</el-button>
    </div>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="deviceCode" label="设备编码" />
      <el-table-column prop="deviceName" label="设备名称" />
      <el-table-column prop="protocol" label="协议" width="100" />
      <el-table-column prop="ip" label="IP" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在线' : '离线' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push('/live')">预览</el-button>
          <el-button type="danger" link @click="onDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="showAdd" title="新增设备" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="设备编码"><el-input v-model="form.deviceCode" /></el-form-item>
        <el-form-item label="设备名称"><el-input v-model="form.deviceName" /></el-form-item>
        <el-form-item label="协议">
          <el-select v-model="form.protocol">
            <el-option label="RTSP" value="RTSP" />
            <el-option label="ONVIF" value="ONVIF" />
            <el-option label="GB28181" value="GB28181" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP"><el-input v-model="form.ip" /></el-form-item>
        <el-form-item label="端口"><el-input-number v-model="form.port" :min="1" :max="65535" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="onAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceList, addDevice, deleteDevice } from '../api'
const list = ref([])
const loading = ref(false)
const showAdd = ref(false)
const form = reactive({ deviceCode: '', deviceName: '', protocol: 'RTSP', ip: '', port: 554 })
async function load() {
  loading.value = true
  try {
    const res = await getDeviceList({ page: 1, size: 50 })
    list.value = res.data?.records || []
  } finally { loading.value = false }
}
async function onAdd() {
  await addDevice(form)
  ElMessage.success('添加成功')
  showAdd.value = false
  load()
}
async function onDelete(id) {
  await ElMessageBox.confirm('确认删除该设备？')
  await deleteDevice(id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>
