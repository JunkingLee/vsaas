# VSaaS 移动端（uni-app）

一套代码支持 **App（iOS/Android）** 与 **微信小程序**（及 H5 调试）。

## 功能

| 页面 | 能力 |
|------|------|
| 登录 | JWT（token / tenantId / userId） |
| 工作台 | 在线统计入口 |
| 设备 | 设备列表 |
| 实时预览 | media 播放地址；**小程序优先 HLS** |
| 录像回放 | record 查询与播放 |
| 告警 | 列表与处理 |
| 我的 | API 地址、退出 |

## HBuilderX（推荐）

1. 安装 HBuilderX，导入本目录 `uni-app`
2. 修改 `utils/config.js` 或登录页 **API 根地址**（App/小程序勿用 127.0.0.1）
3. 运行到浏览器 / 微信开发者工具 / 真机

### 微信小程序

1. `manifest.json` → mp-weixin 填 `appid`
2. 公众平台配置 request/媒体 **HTTPS 合法域名**
3. 预览使用后端返回的 **hls（m3u8）**（SRS 已支持）

### 联调

- 账号：`admin` / `admin123`
- 通道：`1001`，streamKey：`1_1001`
- 接口见仓库 `docs/04-接口设计.md`
