<template>
  <view>
    <view class="banner card">
      <text class="hello">你好，{{ username }}</text>
      <text class="desc">租户 ID：{{ tenantId }}</text>
    </view>
    <view class="stats card" v-if="stats">
      <view class="stat"><text class="num">{{ stats.online || 0 }}</text><text class="label">在线设备</text></view>
      <view class="stat"><text class="num">{{ stats.total || 0 }}</text><text class="label">设备总数</text></view>
      <view class="stat"><text class="num">{{ stats.offline || 0 }}</text><text class="label">离线</text></view>
    </view>
    <view class="grid">
      <view class="item card" @click="go('/pages/device/list', true)">设备列表</view>
      <view class="item card" @click="go('/pages/live/live')">实时预览</view>
      <view class="item card" @click="go('/pages/playback/playback')">录像回放</view>
      <view class="item card" @click="go('/pages/alarm/list', true)">告警中心</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOnlineStats } from '../../api/index.js'

const username = ref(uni.getStorageSync('username') || '用户')
const tenantId = ref(uni.getStorageSync('tenantId') || '-')
const stats = ref(null)

onMounted(async () => {
  try {
    const res = await getOnlineStats()
    stats.value = res.data || res
  } catch (e) {}
})

function go(url, tab) {
  if (tab) uni.switchTab({ url })
  else uni.navigateTo({ url })
}
</script>

<style scoped>
.banner { background: linear-gradient(135deg, #1d2b64, #4a6cf7); color: #fff; }
.hello { font-size: 34rpx; font-weight: 600; display: block; }
.desc { margin-top: 8rpx; opacity: 0.9; font-size: 24rpx; display: block; }
.stats { display: flex; justify-content: space-around; }
.stat { text-align: center; }
.num { font-size: 40rpx; font-weight: 700; color: #1d2b64; display: block; }
.label { font-size: 24rpx; color: #909399; }
.grid { display: flex; flex-wrap: wrap; padding: 0 12rpx; }
.item { width: calc(50% - 36rpx); margin: 12rpx; text-align: center; padding: 40rpx 16rpx; font-size: 28rpx; }
</style>
