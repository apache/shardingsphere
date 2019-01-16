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

package io.shardingsphere.transaction.xa.jta.connection;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.xa.jta.connection.dialect.H2XAConnectionWrapper;
import io.shardingsphere.transaction.xa.jta.connection.dialect.MySQLXAConnectionWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;

/**
 * XA connection factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XAConnectionFactory {
    
    /**
     * Create XA connection from normal connection.
     *
     * @param databaseType database type
     * @param connection normal connection
     * @param xaDataSource XA data source
     * @return XA connection
     */
    public static XAConnection createXAConnection(final DatabaseType databaseType, final XADataSource xaDataSource, final Connection connection) {
        switch (databaseType) {
            case MySQL:
                return new MySQLXAConnectionWrapper().wrap(xaDataSource, connection);
            case H2:
                return new H2XAConnectionWrapper().wrap(xaDataSource, connection);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type: `%s`", databaseType));
        }
    }
}
