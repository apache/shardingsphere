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
  // schema start
  getSchema: (params = {}) => API.get(`/api/schema`, params),
  addSchema: (params = {}) => API.post(`/api/schema`, params),
  getSchemaRule: (schemaName) => API.get(`/api/schema/rule/${schemaName}`),
  putSchemaRule: (schemaName, params = {}) => API.put(`/api/schema/rule/${schemaName}`, params),
  getSchemaDataSource: (schemaName) => API.get(`/api/schema/datasource/${schemaName}`),
  putSchemaDataSource: (schemaName, params = {}) => API.put(`/api/schema/datasource/${schemaName}`, params),
  // Authentication
  getAuth: () => API.get(`/api/authentication`),
  putAuth: (params = {}) => API.put(`/api/authentication`, params),
  // props
  getProps: () => API.get(`/api/props`),
  putProps: (params = {}) => API.put(`/api/props`, params)
}
