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

package org.apache.shardingsphere.infra.metadata.schema.spi;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;
import org.apache.shardingsphere.infra.metadata.database.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.table.TableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Rule meta data loader.
 * 
 * @param <T> type of base rule
 */
public interface RuleMetaDataLoader<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Load schema meta data.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param dataNodes data nodes
     * @param rule rule
     * @param props configuration properties
     * @param excludedTableNames excluded table names
     * @return table name and meta data map
     * @throws SQLException SQL exception
     */
    SchemaMetaData load(DatabaseType databaseType, Map<String, DataSource> dataSourceMap, 
                        DataNodes dataNodes, T rule, ConfigurationProperties props, Collection<String> excludedTableNames) throws SQLException;
    
    /**
     * Load table meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param dataNodes data nodes
     * @param tableName table name
     * @param rule rule
     * @param props configuration properties
     * @return meta data
     * @throws SQLException SQL exception
     */
    Optional<TableMetaData> load(DatabaseType databaseType, Map<String, DataSource> dataSourceMap, DataNodes dataNodes, String tableName, T rule, ConfigurationProperties props) throws SQLException;
}
