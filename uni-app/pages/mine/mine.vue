<template>
  <view>
    <view class="card profile">
      <text class="name">{{ username }}</text>
      <text class="meta">用户 ID：{{ userId }}</text>
      <text class="meta">租户 ID：{{ tenantId }}</text>
    </view>
    <view class="card">
      <text class="label">API 地址</text>
      <input class="input" v-model="apiBase" placeholder="https://api.example.com" />
      <button size="mini" class="btn" @click="saveBase">保存</button>
    </view>
    <view class="card">
      <button class="logout" @click="logout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const username = ref(uni.getStorageSync('username') || '-')
const userId = ref(uni.getStorageSync('userId') || '-')
const tenantId = ref(uni.getStorageSync('tenantId') || '-')
const apiBase = ref(uni.getStorageSync('apiBase') || '')

function saveBase() {
  uni.setStorageSync('apiBase', (apiBase.value || '').replace(/\/$/, ''))
  uni.showToast({ title: '已保存', icon: 'success' })
}

function logout() {
  ;['token', 'tenantId', 'userId', 'username'].forEach((k) => uni.removeStorageSync(k))
  uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<style scoped>
.profile .name { font-size: 34rpx; font-weight: 700; display: block; }
.meta { display: block; margin-top: 8rpx; color: #909399; font-size: 24rpx; }
.label { font-size: 26rpx; color: #606266; }
.input { margin: 12rpx 0; background: #f5f6fa; padding: 16rpx; border-radius: 8rpx; }
.btn { background: #1d2b64; color: #fff; }
.logout { background: #f56c6c; color: #fff; }
</style>
