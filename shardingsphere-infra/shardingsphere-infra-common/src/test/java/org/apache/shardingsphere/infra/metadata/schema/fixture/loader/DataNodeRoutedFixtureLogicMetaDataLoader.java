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

package org.apache.shardingsphere.infra.metadata.schema.fixture.loader;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.DataNodeRoutedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.ShardingSphereMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DataNodeRoutedFixtureLogicMetaDataLoader implements ShardingSphereMetaDataLoader<DataNodeRoutedFixtureRule> {
    
    @Override
    public PhysicalSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                       final DataNodes dataNodes, final DataNodeRoutedFixtureRule rule, final ConfigurationProperties props, final Collection<String> excludedTableNames) {
        Map<String, PhysicalTableMetaData> tables = new HashMap<>(2, 1);
        tables.put("data_node_routed_table_0", new PhysicalTableMetaData(Collections.emptyList(), Collections.emptyList()));
        tables.put("data_node_routed_table_1", new PhysicalTableMetaData(Collections.emptyList(), Collections.emptyList()));
        return new PhysicalSchemaMetaData(tables);
    }
    
    @Override
    public Optional<PhysicalTableMetaData> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                final DataNodes dataNodes, final String tableName, final DataNodeRoutedFixtureRule rule, final ConfigurationProperties props) {
        return ("data_node_routed_table_0".equals(tableName) || "data_node_routed_table_1".equals(tableName))
                ? Optional.of(new PhysicalTableMetaData(Collections.emptyList(), Collections.emptyList())) : Optional.empty();
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
