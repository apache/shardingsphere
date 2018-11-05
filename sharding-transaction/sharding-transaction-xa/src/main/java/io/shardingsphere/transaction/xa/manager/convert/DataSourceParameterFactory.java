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

package io.shardingsphere.transaction.xa.manager.convert;

import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * Get property of common datasource pool then convert to {@code DataSourceParameter}.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceParameterFactory {
    
    /**
     * Create datasource parameter.
     *
     * @param dataSource data source
     * @return datasource parameter
     */
    public static DataSourceParameter build(final DataSource dataSource) {
        switch (PoolType.find(dataSource.getClass().getName())) {
            case HIKARI:
            case DRUID:
            case DBCP:
            case DBCP_TOMCAT:
                return new DBCPDataSourceParameterExtractor(dataSource).extract();
            default:
                return null;
        }
    }
}
