<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>计费中心</h2>
      <div>
        <el-input v-model="billMonth" placeholder="2026-11" style="width:120px;margin-right:8px" />
        <el-button type="primary" @click="onGenerate">生成账单</el-button>
      </div>
    </div>
    <el-table :data="list" border stripe style="margin-top:16px" v-loading="loading">
      <el-table-column prop="billNo" label="账单号" />
      <el-table-column prop="billMonth" label="账期" width="100" />
      <el-table-column prop="totalAmount" label="金额" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已支付' : '未支付' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="生成时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" type="primary" link @click="onPay(row)">支付</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBillList, generateBill } from '../api'
import request from '../utils/request'
const list = ref([])
const loading = ref(false)
const billMonth = ref('2026-11')
async function load() {
  loading.value = true
  try {
    const res = await getBillList({ page: 1, size: 50 })
    list.value = res.data?.records || []
  } finally { loading.value = false }
}
async function onGenerate() {
  await generateBill(billMonth.value)
  ElMessage.success('账单已生成')
  load()
}
async function onPay(row) {
  await request.post('http://127.0.0.1:8007/api/billing/pay/create/' + row.id)
  await request.post('http://127.0.0.1:8007/api/billing/pay/mock/confirm?billId=' + row.id)
  ElMessage.success('模拟支付成功')
  load()
}
onMounted(load)
</script>
