import API from '@/utils/api'

export default {
  // schema start
  getSchema: (params = {}) => API.get(`/api/schema`, params),
  getSchemaRule: (schemaName) => API.get(`/api/schema/rule/${schemaName}`),
  putSchemaRule: (schemaName, params = {}) => API.put(`/api/schema/rule/${schemaName}`, params),
  getSchemaDataSource: (schemaName) => API.get(`/api/schema/datasource/${schemaName}`),
  putSchemaDataSource: (schemaName, params = {}) => API.put(`/api/schema/datasource/${schemaName}`, params)
}
