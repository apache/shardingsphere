/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import axios from 'axios'
import { Message } from 'element-ui'
import C from './conf'

axios.defaults.headers.post['Content-Type'] = 'application/jsoncharset=UTF-8'
axios.defaults.withCredentials = true

function ajax(url, type, options, config) {
  return new Promise((resolve, reject) => {
    axios({
      method: type,
      url: config ? C[config.host] + url : C.HOST + url,
      timeout: 10000,
      headers: {
        'Access-Token': window.localStorage.getItem('Access-Token') || ''
      },
      params: type === 'get' ? options : null,
      data: options
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
            const store = window.localStorage
            store.removeItem('Access-Token')
            store.removeItem('username')
            location.href = '#/login'
            return
          }
          reject(data)
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
  }
}

export default config
