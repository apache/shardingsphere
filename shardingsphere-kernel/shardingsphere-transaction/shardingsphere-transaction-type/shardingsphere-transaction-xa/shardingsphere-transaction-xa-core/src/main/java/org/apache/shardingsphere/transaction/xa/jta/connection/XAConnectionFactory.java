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

package org.apache.shardingsphere.transaction.xa.jta.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * XA connection factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XAConnectionFactory {
    
    static {
        ShardingSphereServiceLoader.register(XAConnectionWrapper.class);
    }
    
    /**
     * Create XA connection from normal connection.
     *
     * @param databaseType database type
     * @param connection normal connection
     * @param xaDataSource XA data source
     * @return XA connection
     * @throws SQLException SQL exception
     */
    public static XAConnection createXAConnection(final DatabaseType databaseType, final XADataSource xaDataSource, final Connection connection) throws SQLException {
        return TypedSPIRegistry.getRegisteredService(XAConnectionWrapper.class, databaseType.getName(), new Properties()).wrap(xaDataSource, connection);
    }
}
