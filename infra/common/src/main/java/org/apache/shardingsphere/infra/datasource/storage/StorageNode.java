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

package org.apache.shardingsphere.infra.datasource.storage;

import com.google.common.base.Objects;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.type.DataSourceAggregatable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

/**
 * Storage node.
 */
@Getter
public final class StorageNode {
    
    private final String name;
    
    private final DatabaseType databaseType;
    
    private final String hostname;
    
    private final int port;
    
    private final String database;
    
    private final String username;
    
    public StorageNode(final DatabaseType databaseType, final JdbcUrl jdbcUrl, final String username) {
        this(null, databaseType, jdbcUrl.getHostname(), jdbcUrl.getPort(), jdbcUrl.getDatabase(), username);
    }
    
    public StorageNode(final DatabaseType databaseType, final String name) {
        this(name, databaseType, null, 0, null, null);
    }
    
    public StorageNode(final String name, final DatabaseType databaseType, final String hostname, final int port, final String database, final String username) {
        this.databaseType = databaseType;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.name = null == name ? generateStorageNodeName() : name;
    }
    
    private String generateStorageNodeName() {
        return databaseType instanceof DataSourceAggregatable ? getStorageNodeName(hostname, port, username) : getStorageNodeName(hostname, port, database, username);
    }
    
    private String getStorageNodeName(final String hostname, final int port, final String username) {
        return String.format("%s_%s_%s", hostname, port, username);
    }
    
    private String getStorageNodeName(final String hostname, final int port, final String database, final String username) {
        return String.format("%s_%s_%s_%s", hostname, port, database, username);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof StorageNode) {
            StorageNode storageNode = (StorageNode) obj;
            return storageNode.hostname.equals(hostname) && storageNode.port == port && storageNode.database.equals(database) && storageNode.username.equals(username);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(name.toUpperCase());
    }
}
