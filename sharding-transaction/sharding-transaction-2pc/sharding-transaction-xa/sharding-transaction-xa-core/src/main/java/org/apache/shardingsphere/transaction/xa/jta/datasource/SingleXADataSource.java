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
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.transaction.xa.jta.connection.SingleXAConnection;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionFactory;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Single XA data source.
 *
 * @author zhaojun
 */
public final class SingleXADataSource extends AbstractUnsupportedSingleXADataSource {
    
    @Getter
    private final String resourceName;
    
    @Getter
    private final XADataSource xaDataSource;
    
    private final DatabaseType databaseType;
    
    private final DataSource originalDataSource;
    
    private final boolean isOriginalXADataSource;
    
    public SingleXADataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource) {
        this.databaseType = databaseType;
        this.resourceName = resourceName;
        originalDataSource = dataSource;
        if (dataSource instanceof XADataSource) {
            xaDataSource = (XADataSource) dataSource;
            isOriginalXADataSource = true;
        } else {
            xaDataSource = XADataSourceFactory.build(databaseType, dataSource);
            isOriginalXADataSource = false;
        }
    }
    
    @Override
    public SingleXAConnection getXAConnection() throws SQLException {
        return isOriginalXADataSource ? getXAConnectionFromXADataSource() : getXAConnectionFromNoneXADataSource();
    }
    
    private SingleXAConnection getXAConnectionFromXADataSource() throws SQLException {
        XAConnection xaConnection = xaDataSource.getXAConnection();
        return new SingleXAConnection(resourceName, xaConnection.getConnection(), xaConnection);
    }
    
    private SingleXAConnection getXAConnectionFromNoneXADataSource() throws SQLException {
        Connection originalConnection = originalDataSource.getConnection();
        XAConnection xaConnection = XAConnectionFactory.createXAConnection(databaseType, xaDataSource, originalConnection);
        return new SingleXAConnection(resourceName, originalConnection, xaConnection);
    }
}
