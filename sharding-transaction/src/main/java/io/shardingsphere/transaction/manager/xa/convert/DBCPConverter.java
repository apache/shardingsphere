/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.manager.xa.convert;

import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;

/**
 * Convert implement of Commons DBCP.
 *
 * @author zhaojun
 */
public final class DBCPConverter implements Converter {
    
    private final DataSourceReflector dataSourceReflector;
    
    public DBCPConverter(final DataSource dataSource) {
        dataSourceReflector = new DataSourceReflector(dataSource);
    }
    
    @Override
    public DataSourceParameter convertTo() {
        DataSourceParameter result = new DataSourceParameter();
        result.setUsername((String) dataSourceReflector.invoke("getUsername"));
        result.setUrl((String) dataSourceReflector.invoke("getUrl"));
        result.setPassword((String) dataSourceReflector.invoke("getPassword"));
        result.setMaximumPoolSize((Integer) dataSourceReflector.invoke("getMaxTotal"));
        return result;
    }
}
