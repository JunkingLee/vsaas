import { http } from '../utils/request.js'

export const login = (data) => http.post('/api/auth/login', data)
export const refreshToken = () => http.post('/api/auth/refresh')
export const getDeviceList = (params) => http.get('/api/device/list', params)
export const getOnlineStats = () => http.get('/api/device/online-stats')
export const getChannelList = (params) => http.get('/api/channel/list', params)
export const getPlayByChannel = (channelId) => http.get('/api/media/play/channel/' + channelId)
export const getPlayByStreamKey = (streamKey) => http.get('/api/media/play/' + encodeURIComponent(streamKey))
export const getAlarmList = (params) => http.get('/api/alarm/list', params)
export const handleAlarm = (id) => http.put('/api/alarm/' + id + '/handle')
export const getRecordFiles = (params) => http.get('/api/record/file/list', params)
export const getRecordPlay = (id) => http.get('/api/record/file/' + id + '/play')
