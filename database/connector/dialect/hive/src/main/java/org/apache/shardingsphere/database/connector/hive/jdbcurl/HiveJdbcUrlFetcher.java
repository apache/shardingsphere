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

package org.apache.shardingsphere.database.connector.hive.jdbcurl;

import org.apache.hive.jdbc.HiveConnection;
import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectJdbcUrlFetcher;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC URL fetcher for Hive.
 */
public final class HiveJdbcUrlFetcher implements DialectJdbcUrlFetcher {
    
    @Override
    public String fetch(final Connection connection) throws SQLException {
        return connection.unwrap(HiveConnection.class).getConnectedUrl();
    }
    
    @Override
    public Class<? extends Connection> getConnectionClass() {
        return HiveConnection.class;
    }
}
