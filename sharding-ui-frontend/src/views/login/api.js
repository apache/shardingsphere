import API from '@/utils/api'

export default {
  getLogin: (params = {}) => API.post(`/api/login`, params)
}
