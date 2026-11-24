<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">VSaaS</div>
      <el-menu :default-active="$route.path" router background-color="#001529" text-color="#fff" active-text-color="#409EFF">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon>仪表盘</el-menu-item>
        <el-menu-item index="/device"><el-icon><Monitor /></el-icon>设备管理</el-menu-item>
        <el-menu-item index="/live"><el-icon><VideoCamera /></el-icon>实时预览</el-menu-item>
        <el-menu-item index="/playback"><el-icon><VideoPlay /></el-icon>录像回放</el-menu-item>
        <el-menu-item index="/alarm"><el-icon><Bell /></el-icon>告警中心</el-menu-item>
        <el-menu-item index="/billing"><el-icon><Wallet /></el-icon>计费中心</el-menu-item>
        <el-menu-item index="/user"><el-icon><User /></el-icon>用户权限</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ username }}</span>
        <el-button type="danger" link @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const username = computed(() => localStorage.getItem('username') || '用户')
function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}
.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #eee;
}
</style>
