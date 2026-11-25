<template>
  <view>
    <view class="card" v-for="a in list" :key="a.id">
      <view class="row">
        <text class="type">{{ a.alarmType || '告警' }}</text>
        <text :class="['tag', a.status === 1 ? 'done' : 'todo']">{{ a.status === 1 ? '已处理' : '未处理' }}</text>
      </view>
      <text class="content">{{ a.alarmContent || '-' }}</text>
      <text class="meta">通道 {{ a.channelId }} · {{ a.alarmTime || a.createdAt }}</text>
      <button v-if="a.status !== 1" size="mini" class="btn" @click="onHandle(a)">处理</button>
    </view>
    <view v-if="!list.length" class="empty">暂无告警</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAlarmList, handleAlarm } from '../../api/index.js'

const list = ref([])

async function load() {
  try {
    const res = await getAlarmList({ page: 1, size: 50 })
    const data = res.data || {}
    list.value = data.records || data.list || data || []
    if (!Array.isArray(list.value)) list.value = []
  } catch (e) {
    list.value = []
  }
}

async function onHandle(a) {
  try {
    await handleAlarm(a.id)
    uni.showToast({ title: '已处理', icon: 'success' })
    load()
  } catch (e) {}
}

onShow(load)
</script>

<style scoped>
.row { display: flex; justify-content: space-between; }
.type { font-weight: 600; font-size: 28rpx; }
.content { display: block; margin-top: 12rpx; }
.meta { display: block; margin-top: 8rpx; color: #909399; font-size: 24rpx; }
.tag { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.todo { background: #fef0f0; color: #f56c6c; }
.done { background: #e1f3d8; color: #67c23a; }
.btn { margin-top: 16rpx; background: #1d2b64; color: #fff; }
.empty { text-align: center; color: #909399; padding: 80rpx; }
</style>
