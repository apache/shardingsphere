import API from '@/utils/api'

export default {
  getOrcheData: (params = {}) => API.get(`/api/orchestration/datasource`, params),
  putOrcheData: (params = {}) => API.put(`/api/orchestration/datasource`, params),
  getOrcheInstance: (params = {}) => API.get(`/api/orchestration/instance`, params),
  putOrcheInstance: (params = {}) => API.put(`/api/orchestration/instance`, params)
}