<template>
  <view>
    <view class="card">
      <text class="label">通道 ID</text>
      <input class="input" type="number" v-model="channelId" placeholder="例如 1001" />
      <button class="btn-primary" size="mini" :loading="loading" @click="loadPlay">获取预览</button>
    </view>
    <view class="player card" v-if="playUrl">
      <video class="video" :src="playUrl" controls autoplay object-fit="contain" />
      <text class="url">{{ playUrl }}</text>
    </view>
    <view class="card tip">
      <text>微信小程序请优先使用 HLS（m3u8），并配置 HTTPS 合法域名。</text>
      <text>streamKey：{tenantId}_{channelId}，推流：rtmp://host/live/1_1001</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPlayByChannel } from '../../api/index.js'

const channelId = ref('1001')
const playUrl = ref('')
const loading = ref(false)

onLoad((q) => {
  if (q.channelId) channelId.value = q.channelId
})

async function loadPlay() {
  if (!channelId.value) {
    uni.showToast({ title: '请输入通道ID', icon: 'none' })
    return
  }
  loading.value = true
  playUrl.value = ''
  try {
    const res = await getPlayByChannel(channelId.value)
    const data = res.data || res || {}
    // 小程序/通用：优先 HLS
    playUrl.value = data.hls || data.flv || data.playUrl || data.url || ''
    if (!playUrl.value) uni.showToast({ title: '未返回播放地址', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.label { font-size: 26rpx; color: #606266; }
.input { margin: 16rpx 0; background: #f5f6fa; padding: 16rpx 20rpx; border-radius: 8rpx; }
.btn-primary { background: #1d2b64; color: #fff; }
.video { width: 100%; height: 420rpx; background: #000; }
.url { display: block; margin-top: 12rpx; font-size: 22rpx; color: #909399; word-break: break-all; }
.tip text { display: block; font-size: 24rpx; color: #909399; line-height: 1.6; margin-bottom: 8rpx; }
</style>
