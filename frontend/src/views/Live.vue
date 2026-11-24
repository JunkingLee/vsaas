<template>
  <div>
    <h2>实时预览</h2>
    <el-form inline style="margin: 16px 0">
      <el-form-item label="通道ID">
        <el-input v-model="channelId" placeholder="例如 1001" style="width:140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadPlay">获取地址并播放</el-button>
        <el-button @click="stopPlay">停止</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="16">
      <el-col :span="16">
        <div class="player-box">
          <video ref="videoRef" controls autoplay muted style="width:100%;max-height:480px;background:#000"></video>
        </div>
      </el-col>
      <el-col :span="8">
        <el-card v-if="urls">
          <template #header>播放地址</template>
          <p><b>FLV:</b> {{ urls.flv }}</p>
          <p><b>HLS:</b> {{ urls.hls }}</p>
          <p><b>streamKey:</b> {{ urls.streamKey }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const channelId = ref('')
const urls = ref(null)
const videoRef = ref(null)
let flvPlayer = null
async function loadPlay() {
  if (!channelId.value) { ElMessage.warning('请输入通道ID'); return }
  stopPlay()
  try {
    const res = await request.get('http://127.0.0.1:8003/api/media/play/channel/' + channelId.value)
    urls.value = res.data
    await ensureFlv()
    if (window.flvjs && window.flvjs.isSupported() && urls.value.flv) {
      flvPlayer = window.flvjs.createPlayer({ type: 'flv', url: urls.value.flv, isLive: true })
      flvPlayer.attachMediaElement(videoRef.value)
      flvPlayer.load()
      flvPlayer.play()
    }
  } catch (e) {}
}
function stopPlay() {
  if (flvPlayer) {
    flvPlayer.pause(); flvPlayer.unload(); flvPlayer.detachMediaElement(); flvPlayer.destroy(); flvPlayer = null
  }
  if (videoRef.value) videoRef.value.src = ''
}
function ensureFlv() {
  return new Promise((resolve) => {
    if (window.flvjs) return resolve()
    const s = document.createElement('script')
    s.src = 'https://cdn.jsdelivr.net/npm/flv.js@1.6.2/dist/flv.min.js'
    s.onload = resolve
    document.head.appendChild(s)
  })
}
onBeforeUnmount(stopPlay)
</script>

<style scoped>
.player-box { border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; }
</style>
