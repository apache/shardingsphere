import API from '@/utils/api'

export default {
  getRegCenter: (params = {}) => API.get(`/api/reg-center`, params),
  deleteRegCenter: (params = {}) => API.delete(`/api/reg-center`, params),
  postRegCenter: (params = {}) => API.post(`/api/reg-center`, params),
  getRegCenterActivated: (params = {}) => API.get(`/api/reg-center/activated`, params),
  postRegCenterConnect: (params = {}) => API.post(`/api/reg-center/connect`, params)
}
