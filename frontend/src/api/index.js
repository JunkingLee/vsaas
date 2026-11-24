import request from '../utils/request'

const AUTH = 'http://127.0.0.1:8001'
const DEVICE = 'http://127.0.0.1:8002'
const MEDIA = 'http://127.0.0.1:8003'
const RECORD = 'http://127.0.0.1:8005'
const ALARM = 'http://127.0.0.1:8006'
const BILLING = 'http://127.0.0.1:8007'
const USER = 'http://127.0.0.1:8008'

export const login = (data) => request.post(AUTH + '/api/auth/login', data)
export const refreshToken = () => request.post(AUTH + '/api/auth/refresh')

export const getDeviceList = (params) => request.get(DEVICE + '/api/device/list', { params })
export const addDevice = (data) => request.post(DEVICE + '/api/device', data)
export const deleteDevice = (id) => request.delete(DEVICE + '/api/device/' + id)
export const getOnlineStats = () => request.get(DEVICE + '/api/device/online-stats')
export const getChannelList = (params) => request.get(DEVICE + '/api/channel/list', { params })

export const getPlayUrls = (streamKey) => request.get(MEDIA + '/api/media/play/' + streamKey)

export const getAlarmList = (params) => request.get(ALARM + '/api/alarm/list', { params })
export const handleAlarm = (id) => request.put(ALARM + '/api/alarm/' + id + '/handle')

export const getBillList = (params) => request.get(BILLING + '/api/billing/bill/list', { params })
export const generateBill = (billMonth) => request.post(BILLING + '/api/billing/bill/generate?billMonth=' + billMonth)

export const getUserList = (params) => request.get(USER + '/api/user/list', { params })
export const getRoleList = (params) => request.get(USER + '/api/role/list', { params })
