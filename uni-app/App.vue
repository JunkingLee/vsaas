<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  }
}
</script>

<style>
page {
  background-color: #f5f6fa;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}
.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.btn-primary {
  background: #1d2b64;
  color: #fff;
  border-radius: 8rpx;
}
</style>
