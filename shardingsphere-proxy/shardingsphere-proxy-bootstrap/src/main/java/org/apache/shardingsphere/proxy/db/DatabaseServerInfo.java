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

package org.apache.shardingsphere.proxy.db;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Database server info.
 */
@Getter
public final class DatabaseServerInfo {
    
    private final String databaseName;
    
    private final String databaseVersion;
    
    public DatabaseServerInfo(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            databaseName = databaseMetaData.getDatabaseProductName();
            databaseVersion = databaseMetaData.getDatabaseProductVersion();
        } catch (final SQLException ex) {
            throw new ShardingSphereException("Load database server info failed:", ex);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Database name is `%s`, version is `%s`", databaseName, databaseVersion);
    }
}
