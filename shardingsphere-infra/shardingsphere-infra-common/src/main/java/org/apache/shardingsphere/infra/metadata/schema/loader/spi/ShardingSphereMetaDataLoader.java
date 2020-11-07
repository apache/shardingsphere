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

package org.apache.shardingsphere.infra.metadata.schema.loader.spi;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere meta data loader.
 * 
 * @param <T> type of base rule
 */
public interface ShardingSphereMetaDataLoader<T extends TableContainedRule> extends OrderedSPI<T> {
    
    /**
     * Load table meta data.
     *
     * @param tableName table name
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param dataNodes data nodes
     * @param rule rule
     * @param props configuration properties
     * @return meta data
     * @throws SQLException SQL exception
     */
    Optional<PhysicalTableMetaData> load(String tableName, 
                                         DatabaseType databaseType, Map<String, DataSource> dataSourceMap, DataNodes dataNodes, T rule, ConfigurationProperties props) throws SQLException;
}
