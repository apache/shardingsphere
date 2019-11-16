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

package org.apache.shardingsphere.transaction.xa.jta.datasource;

import lombok.Getter;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionFactory;
import org.apache.shardingsphere.transaction.xa.jta.connection.XATransactionConnection;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * XA transaction data source.
 *
 * @author zhaojun
 */
public final class XATransactionDataSource {
    
    @Getter
    private final String resourceName;
    
    @Getter
    private final XADataSource xaDataSource;
    
    private final DatabaseType databaseType;
    
    private final DataSource originalDataSource;
    
    public XATransactionDataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource) {
        this.databaseType = databaseType;
        this.resourceName = resourceName;
        originalDataSource = dataSource;
        xaDataSource = XADataSourceFactory.build(databaseType, dataSource);
    }
    
    /**
     * Get XA connection.
     *
     * @return XA transaction connection
     * @throws SQLException SQL exception
     */
    public XATransactionConnection getConnection() throws SQLException {
        Connection originalConnection = originalDataSource.getConnection();
        XAConnection xaConnection = XAConnectionFactory.createXAConnection(databaseType, xaDataSource, originalConnection);
        return new XATransactionConnection(resourceName, originalConnection, xaConnection);
    }
}
