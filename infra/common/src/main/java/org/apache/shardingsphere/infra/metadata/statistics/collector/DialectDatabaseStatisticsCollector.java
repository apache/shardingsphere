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

package org.apache.shardingsphere.infra.metadata.statistics.collector;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Dialect database statistics collector.
 */
@SingletonSPI
public interface DialectDatabaseStatisticsCollector extends DatabaseTypedSPI {
    
    /**
     * Collect row column values.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param metaData shardingsphere meta data
     * @return row column datas
     */
    Optional<Collection<Map<String, Object>>> collectRowColumnValues(String databaseName, String schemaName, String tableName, ShardingSphereMetaData metaData);
    
    /**
     * Is statistics tables.
     *
     * @param schemaTables schema tables
     * @return returns true if all are statistics tables
     */
    boolean isStatisticsTables(Map<String, Collection<String>> schemaTables);
}
