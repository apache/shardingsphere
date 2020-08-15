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

package org.apache.shardingsphere.infra.metadata.fixture.loader;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.fixture.rule.DataNodeRoutedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DataNodeRoutedFixtureRuleMetaDataLoader implements RuleMetaDataLoader<DataNodeRoutedFixtureRule> {
    
    @Override
    public SchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                               final DataNodes dataNodes, final DataNodeRoutedFixtureRule rule, final ConfigurationProperties props, final Collection<String> excludedTableNames) {
        Map<String, TableMetaData> tables = new HashMap<>(2, 1);
        tables.put("data_node_routed_table_0", new TableMetaData(Collections.emptyList(), Collections.emptyList()));
        tables.put("data_node_routed_table_1", new TableMetaData(Collections.emptyList(), Collections.emptyList()));
        return new SchemaMetaData(tables);
    }
    
    @Override
    public Optional<TableMetaData> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                        final DataNodes dataNodes, final String tableName, final DataNodeRoutedFixtureRule rule, final ConfigurationProperties props) {
        return ("data_node_routed_table_0".equals(tableName) || "data_node_routed_table_1".equals(tableName))
                ? Optional.of(new TableMetaData(Collections.emptyList(), Collections.emptyList())) : Optional.empty();
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<DataNodeRoutedFixtureRule> getTypeClass() {
        return DataNodeRoutedFixtureRule.class;
    }
}
