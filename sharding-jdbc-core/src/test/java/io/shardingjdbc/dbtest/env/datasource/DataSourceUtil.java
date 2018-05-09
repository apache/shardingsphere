/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.env.datasource;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.dbtest.env.IntegrateTestEnvironment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * Data source utility.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceUtil {
    
    /**
     * Create data source.
     * 
     * @param databaseType data base type
     * @param dataSourceName data source name
     * @return data source
     */
    public static DataSource createDataSource(final DatabaseType databaseType, final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        DatabaseEnvironment databaseEnvironment = IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().get(databaseType);
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setUrl(databaseEnvironment.getURL(dataSourceName));
        result.setUsername(databaseEnvironment.getUsername());
        result.setPassword(databaseEnvironment.getPassword());
        result.setMaxTotal(1);
        result.setValidationQuery("SELECT 1");
        if (DatabaseType.Oracle == databaseType) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dataSourceName));
        }
        return result;
    }
}
