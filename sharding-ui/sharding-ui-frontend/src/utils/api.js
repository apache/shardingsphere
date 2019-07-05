import axios from 'axios'
import jsonp from 'jsonp'
import { Message } from 'element-ui'
import C from './conf'

axios.defaults.headers.post['Content-Type'] = 'application/jsoncharset=UTF-8'
axios.defaults.withCredentials = true

const configData = (type, params) => {
  if (type === 'post') {
    return params
  } else if (type === 'put') {
    return params
  } else if (type === 'delete') {
    return params
  }
  return null
}

function ajax(url, type, options) {
  return new Promise((resolve, reject) => {
    axios({
      method: type,
      url: C.HOST + url,
      timeout: 10000,
      headers: {
        'Access-Token': window.localStorage.getItem('Access-Token') || ''
      },
      params: type === 'get' ? options : null,
      data: configData(type, options)
    })
      .then(result => {
        const data = result.data
        const success = data.success
        if (success) {
          resolve(data)
          return
        }

        if (!success) {
          if (data.errorCode === 403) {
            location.href = '#/login'
            return
          }
          Message({
            message: data.errorMsg,
            type: 'error',
            duration: 2 * 1000
          })
          return
        }
      })
      .catch(error => {
        Message({
          message: error,
          type: 'error',
          duration: 2 * 1000
        })
      })
  })
}

const config = {
  get(url, options, config) {
    return new Promise((resolve, reject) => {
      ajax(url, 'get', options, config).then(
        data => {
          resolve(data)
        },
        error => {
          reject(error)
        }
      )
    })
  },

  post(url, options, config) {
    return new Promise((resolve, reject) => {
      ajax(url, 'post', options, config).then(
        data => {
          resolve(data)
        },
        error => {
          reject(error)
        }
      )
    })
  },

  put(url, options) {
    return new Promise((resolve, reject) => {
      ajax(url, 'put', options).then(
        data => {
          resolve(data)
        },
        error => {
          reject(error)
        }
      )
    })
  },

  delete(url, options) {
    return new Promise((resolve, reject) => {
      ajax(url, 'delete', options).then(
        data => {
          resolve(data)
        },
        error => {
          reject(error)
        }
      )
    })
  },

  jsonp(url, options) {
    return new Promise((resolve, reject) => {
      jsonp(`${C.JSONP_HOST}${url}${options}`, null, (err, data) => {
        if (err) {
          reject(err)
        } else {
          resolve(data)
        }
      })
    })
  }
}

export default config
