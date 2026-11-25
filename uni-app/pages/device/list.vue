<template>
  <view>
    <view class="card" v-for="d in list" :key="d.id" @click="openLive(d)">
      <view class="row">
        <text class="name">{{ d.deviceName || d.deviceCode }}</text>
        <text :class="['tag', d.status === 1 ? 'on' : 'off']">{{ d.status === 1 ? '在线' : '离线' }}</text>
      </view>
      <text class="meta">{{ d.protocol }} · {{ d.ip || '-' }}:{{ d.port || '-' }}</text>
      <text class="meta">编码：{{ d.deviceCode }}</text>
    </view>
    <view v-if="!list.length && !loading" class="empty">暂无设备</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getDeviceList } from '../../api/index.js'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getDeviceList({ page: 1, size: 50 })
    const data = res.data || {}
    list.value = data.records || data.list || data || []
    if (!Array.isArray(list.value)) list.value = []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openLive(d) {
  uni.navigateTo({ url: '/pages/live/live?deviceId=' + d.id })
}

onShow(load)
</script>

<style scoped>
.row { display: flex; justify-content: space-between; align-items: center; }
.name { font-size: 30rpx; font-weight: 600; }
.meta { display: block; margin-top: 8rpx; color: #909399; font-size: 24rpx; }
.tag { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.on { background: #e1f3d8; color: #67c23a; }
.off { background: #f4f4f5; color: #909399; }
.empty { text-align: center; color: #909399; padding: 80rpx; }
</style>
