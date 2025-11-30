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

package org.apache.shardingsphere.infra.metadata.statistics.collector.opengauss;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.PostgreSQLStatisticsCollector;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Statistics collector for openGauss.
 */
public final class OpenGaussStatisticsCollector implements DialectDatabaseStatisticsCollector {
    
    private final PostgreSQLStatisticsCollector delegate = new PostgreSQLStatisticsCollector();
    
    @Override
    public Optional<Collection<Map<String, Object>>> collectRowColumnValues(final String databaseName, final String schemaName, final String tableName, final ShardingSphereMetaData metaData) {
        return delegate.collectRowColumnValues(databaseName, schemaName, tableName, metaData);
    }
    
    @Override
    public boolean isStatisticsTables(final Map<String, Collection<String>> schemaTables) {
        return delegate.isStatisticsTables(schemaTables);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
