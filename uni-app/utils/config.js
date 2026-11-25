/**
 * H5 可空 base；App/小程序必须填可访问的 API 根（HTTPS）
 * 不要使用 127.0.0.1
 */
// #ifdef H5
export const API_BASE = ''
// #endif

// #ifndef H5
export const API_BASE = 'https://your-api.example.com'
// #endif
