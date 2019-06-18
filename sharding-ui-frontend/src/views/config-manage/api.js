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
  // configMap
  getConfigMap: () => API.get(`/api/config-map`),
  putConfigMap: (params = {}) => API.put(`/api/config-map`, params),
  // props
  getProps: () => API.get(`/api/props`),
  putProps: (params = {}) => API.put(`/api/props`, params)
}
