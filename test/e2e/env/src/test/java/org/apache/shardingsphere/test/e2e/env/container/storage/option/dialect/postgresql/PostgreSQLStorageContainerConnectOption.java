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

package org.apache.shardingsphere.test.e2e.env.container.storage.option.dialect.postgresql;

import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;

/**
 * Storage container connect option for PostgreSQL.
 */
public final class PostgreSQLStorageContainerConnectOption implements StorageContainerConnectOption {
    
    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }
    
    @Override
    public String getURL(final String host, final int port) {
        return String.format("jdbc:postgresql://%s:%s/?ssl=on&sslmode=prefer", host, port);
    }
    
    @Override
    public String getURL(final String host, final int port, final String dataSourceName) {
        return String.format("jdbc:postgresql://%s:%s/%s?ssl=on&sslmode=prefer", host, port, dataSourceName);
    }
}
