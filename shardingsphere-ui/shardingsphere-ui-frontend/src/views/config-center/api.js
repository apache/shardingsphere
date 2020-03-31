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

import API from '@/utils/api'

export default {
  getConfigCenter: (params = {}) => API.get(`/api/config-center`, params),
  deleteConfigCenter: (params = {}) => API.delete(`/api/config-center`, params),
  postConfigCenter: (params = {}) => API.post(`/api/config-center`, params),
  getConfigCenterActivated: (params = {}) => API.get(`/api/config-center/activated`, params),
  postConfigCenterConnect: (params = {}) => API.post(`/api/config-center/connect`, params),
  updateConfigCenter: (config = {}) => API.post(`/api/config-center/update`, config)
}
