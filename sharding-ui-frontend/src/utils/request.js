import axios from 'axios'
import { Message } from 'element-ui'

const service = axios.create({
  baseURL: process.env.BASE_API,
  timeout: 50000
})

service.interceptors.request.use(
  config => {
    // todo
  },
  error => {
    console.log(error)
    Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {

  },
  error => {
    console.log(error)
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
