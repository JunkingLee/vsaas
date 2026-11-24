<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="list" border stripe style="margin-top:16px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList } from '../api'
const list = ref([])
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: 1, size: 50 })
    list.value = res.data?.records || []
  } finally { loading.value = false }
})
</script>
