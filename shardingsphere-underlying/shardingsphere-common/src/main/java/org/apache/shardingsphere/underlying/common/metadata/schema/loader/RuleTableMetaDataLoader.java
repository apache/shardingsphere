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

package org.apache.shardingsphere.underlying.common.metadata.schema.loader;

import org.apache.shardingsphere.spi.order.OrderedSPI;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Rule table meta data loader.
 * 
 * @param <T> type of base rule
 */
public interface RuleTableMetaDataLoader<T extends BaseRule> extends OrderedSPI<Class<T>> {
    
    /**
     * Load meta data.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param rule rule
     * @param properties configuration properties
     * @return table name and meta data map
     * @throws SQLException SQL exception
     */
    // TODO add exclude tables
    Map<String, TableMetaData> load(DatabaseType databaseType, Map<String, DataSource> dataSourceMap, T rule, ConfigurationProperties properties) throws SQLException;
    
    /**
     * Load meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param tableName table name
     * @param rule rule
     * @param properties configuration properties
     * @return meta data
     * @throws SQLException SQL exception
     */
    Optional<TableMetaData> load(DatabaseType databaseType, Map<String, DataSource> dataSourceMap, String tableName, T rule, ConfigurationProperties properties) throws SQLException;
}
