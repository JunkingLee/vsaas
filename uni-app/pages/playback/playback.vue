<template>
  <view>
    <view class="card">
      <text class="label">通道 ID</text>
      <input class="input" type="number" v-model="channelId" placeholder="1001" />
      <button class="btn-primary" size="mini" :loading="loading" @click="search">查询录像</button>
    </view>
    <view class="card" v-for="f in files" :key="f.id" @click="play(f)">
      <text class="name">片段 #{{ f.id }}</text>
      <text class="meta">{{ f.startTime }} ~ {{ f.endTime }}</text>
    </view>
    <view v-if="playUrl" class="card">
      <video class="video" :src="playUrl" controls autoplay />
    </view>
    <view v-if="!files.length && searched" class="empty">暂无录像</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getRecordFiles, getRecordPlay } from '../../api/index.js'

const channelId = ref('1001')
const files = ref([])
const loading = ref(false)
const searched = ref(false)
const playUrl = ref('')

async function search() {
  loading.value = true
  searched.value = true
  playUrl.value = ''
  try {
    const res = await getRecordFiles({ channelId: channelId.value, page: 1, size: 30 })
    const data = res.data || {}
    files.value = data.records || data.list || data || []
    if (!Array.isArray(files.value)) files.value = []
  } catch (e) {
    files.value = []
  } finally {
    loading.value = false
  }
}

async function play(f) {
  try {
    const res = await getRecordPlay(f.id)
    const data = res.data || res || {}
    playUrl.value = data.url || data.playUrl || data.hls || ''
    if (!playUrl.value) uni.showToast({ title: '无播放地址', icon: 'none' })
  } catch (e) {}
}
</script>

<style scoped>
.label { font-size: 26rpx; color: #606266; }
.input { margin: 16rpx 0; background: #f5f6fa; padding: 16rpx 20rpx; border-radius: 8rpx; }
.btn-primary { background: #1d2b64; color: #fff; }
.name { font-size: 28rpx; font-weight: 600; display: block; }
.meta { display: block; color: #909399; font-size: 24rpx; margin-top: 6rpx; }
.video { width: 100%; height: 400rpx; background: #000; }
.empty { text-align: center; color: #909399; padding: 60rpx; }
</style>
