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

package io.shardingsphere.transaction.manager.xa.fixture;

import io.shardingsphere.core.constant.PoolType;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DataSourceUtils {
    
    public static DataSource build(final PoolType poolType) {
        switch (poolType) {
            case DBCP_TOMCAT:
                return newBasicDataSource();
            default:
                return null;
        }
    }
    
    private static BasicDataSource newBasicDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setUrl("jdbc:mysql://localhost:3306");
        result.setMaxTotal(10);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
}
