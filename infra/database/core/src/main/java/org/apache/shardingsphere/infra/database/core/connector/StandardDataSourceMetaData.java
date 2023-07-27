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

package org.apache.shardingsphere.infra.database.core.connector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Standard data source meta data.
 */
@RequiredArgsConstructor
@Getter
public final class StandardDataSourceMetaData implements DataSourceMetaData {
    
    private final String hostname;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Properties queryProperties;
    
    private final Properties defaultQueryProperties;
    
    public StandardDataSourceMetaData(final String hostname, final int port, final String catalog, final String schema) {
        this(hostname, port, catalog, schema, new Properties(), new Properties());
    }
    
    /**
     * Judge whether two of data sources are in the same database instance.
     *
     * @param dataSourceMetaData data source meta data
     * @return data sources are in the same database instance or not
     */
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return hostname.equals(dataSourceMetaData.getHostname()) && port == dataSourceMetaData.getPort();
    }
}
