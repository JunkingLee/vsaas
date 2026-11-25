<template>
  <view class="page">
    <view class="hero">
      <text class="title">VSaaS 监控</text>
      <text class="sub">视频监控云 · App / 小程序</text>
    </view>
    <view class="form card">
      <input class="input" v-model="form.username" placeholder="用户名" />
      <input class="input" v-model="form.password" password placeholder="密码" />
      <input class="input" v-model="apiBase" placeholder="API 地址(可选，App调试填局域网)" />
      <button class="btn-primary" :loading="loading" @click="onLogin">登录</button>
      <text class="tip">演示账号 admin / admin123</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { login } from '../../api/index.js'

const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const apiBase = ref(uni.getStorageSync('apiBase') || '')

async function onLogin() {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' })
    return
  }
  if (apiBase.value) {
    uni.setStorageSync('apiBase', apiBase.value.replace(/\/$/, ''))
  }
  loading.value = true
  try {
    const res = await login(form)
    const d = res.data || {}
    uni.setStorageSync('token', d.token)
    uni.setStorageSync('tenantId', d.tenantId)
    uni.setStorageSync('userId', d.userId)
    uni.setStorageSync('username', d.username || form.username)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 400)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: linear-gradient(160deg, #1d2b64, #4a6cf7 50%, #f5f6fa 50%); }
.hero { padding: 160rpx 48rpx 48rpx; color: #fff; }
.title { font-size: 48rpx; font-weight: 700; display: block; }
.sub { margin-top: 12rpx; opacity: 0.85; font-size: 26rpx; display: block; }
.input { background: #f5f6fa; border-radius: 8rpx; padding: 22rpx 24rpx; margin-bottom: 20rpx; font-size: 28rpx; }
.btn-primary { margin-top: 12rpx; background: #1d2b64; color: #fff; border-radius: 8rpx; }
.tip { display: block; text-align: center; color: #909399; font-size: 24rpx; margin-top: 24rpx; }
</style>
