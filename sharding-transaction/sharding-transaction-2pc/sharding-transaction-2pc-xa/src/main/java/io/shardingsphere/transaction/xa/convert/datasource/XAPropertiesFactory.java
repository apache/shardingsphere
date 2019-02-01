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

package io.shardingsphere.transaction.xa.convert.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.H2XAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.MySQLXAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.OracleXAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.PostgreSQLXAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.SQLServerXAProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * XA properties factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XAPropertiesFactory {
    
    /**
     * Create XA properties.
     * 
     * @param databaseType database type
     * @return XA properties
     */
    public static XAProperties createXAProperties(final DatabaseType databaseType) {
        switch (databaseType) {
            case H2:
                return new H2XAProperties();
            case MySQL:
                return new MySQLXAProperties();
            case PostgreSQL:
                return new PostgreSQLXAProperties();
            case Oracle:
                return new OracleXAProperties();
            case SQLServer:
                return new SQLServerXAProperties();
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type: `%s`", databaseType));
        }
    }
}
