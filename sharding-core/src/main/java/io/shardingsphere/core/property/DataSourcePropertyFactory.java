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

package io.shardingsphere.core.property;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.property.dialect.H2DataSourcePropertyParser;
import io.shardingsphere.core.property.dialect.MySQLDataSourcePropertyParser;
import io.shardingsphere.core.property.dialect.OracleDataSourcePropertyParser;
import io.shardingsphere.core.property.dialect.PostgreSQLDataSourcePropertyParser;
import io.shardingsphere.core.property.dialect.SQLServerDataSourcePropertyParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Data source property factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePropertyFactory {
    
    /**
     * Create data source property parser.
     *
     * @param databaseType data base type.
     * @return data source property parser.
     */
    public static DataSourcePropertyParser createDataSourcePropertyParser(final DatabaseType databaseType) {
        switch (databaseType) {
            case H2:
                return new H2DataSourcePropertyParser();
            case MySQL:
                return new MySQLDataSourcePropertyParser();
            case Oracle:
                return new OracleDataSourcePropertyParser();
            case PostgreSQL:
                return new PostgreSQLDataSourcePropertyParser();
            case SQLServer:
                return new SQLServerDataSourcePropertyParser();
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
}
