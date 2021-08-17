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
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public final class DataNodeContainedFixtureRuleBasedTableMetaDataBuilder implements RuleBasedTableMetaDataBuilder<DataNodeContainedFixtureRule> {
    
    @Override
    public Optional<TableMetaData> load(final String tableName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                        final DataNodes dataNodes, final DataNodeContainedFixtureRule rule, final ConfigurationProperties props) {
        return ("data_node_routed_table1".equals(tableName) || "data_node_routed_table2".equals(tableName))
                ? Optional.of(new TableMetaData(tableName, Collections.emptyList(), Collections.emptyList())) : Optional.empty();
    }
    
    @Override
    public Map<String, TableMetaData> load(final Collection<String> tableNames, final DataNodeContainedFixtureRule rule, final SchemaBuilderMaterials materials,
                                           final ExecutorService executorService) throws SQLException {
        if (!tableNames.isEmpty() && (tableNames.contains("data_node_routed_table1") || tableNames.contains("data_node_routed_table2"))) {
            Map<String, TableMetaData> result = new LinkedHashMap<>();
            for (String tableName : tableNames) {
                result.put(tableName, new TableMetaData(tableName, Collections.emptyList(), Collections.emptyList()));
            }
            return result;
        }
        return Collections.emptyMap();
    }
    
    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final DataNodeContainedFixtureRule rule) {
        ColumnMetaData columnMetaData = new ColumnMetaData("id", 1, true, true, false);
        return new TableMetaData(tableName, Collections.singletonList(columnMetaData), Collections.emptyList());
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<DataNodeContainedFixtureRule> getTypeClass() {
        return DataNodeContainedFixtureRule.class;
    }
}
