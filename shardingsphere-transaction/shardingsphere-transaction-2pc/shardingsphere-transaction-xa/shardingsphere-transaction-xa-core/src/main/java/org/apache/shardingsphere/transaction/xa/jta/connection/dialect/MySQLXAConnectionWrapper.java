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

package org.apache.shardingsphere.transaction.xa.jta.connection.dialect;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * XA connection wrapper for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLXAConnectionWrapper implements XAConnectionWrapper {
    
    private static final String MYSQL_XA_DATASOURCE_5 = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
    
    private static final String MYSQL_XA_DATASOURCE_8 = "com.mysql.cj.jdbc.MysqlXADataSource";
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Override
    public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) {
        Connection physicalConnection = unwrapPhysicalConnection(xaDataSource.getClass().getName(), connection);
        Method method = xaDataSource.getClass().getDeclaredMethod("wrapConnection", Connection.class);
        method.setAccessible(true);
        return (XAConnection) method.invoke(xaDataSource, physicalConnection);
    }
    
    @SneakyThrows({SQLException.class, ClassNotFoundException.class})
    private Connection unwrapPhysicalConnection(final String xaDataSourceClassName, final Connection connection) {
        switch (xaDataSourceClassName) {
            case MYSQL_XA_DATASOURCE_5:
                return (Connection) connection.unwrap(Class.forName("com.mysql.jdbc.Connection"));
            case MYSQL_XA_DATASOURCE_8:
                return (Connection) connection.unwrap(Class.forName("com.mysql.cj.jdbc.JdbcConnection"));
            default:
                throw new UnsupportedOperationException(String.format("Cannot support xa datasource: `%s`", xaDataSourceClassName));
        }
    }
}
