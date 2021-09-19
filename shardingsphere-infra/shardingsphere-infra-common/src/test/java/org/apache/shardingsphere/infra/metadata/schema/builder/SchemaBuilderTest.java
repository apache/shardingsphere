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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaBuilderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    @Test
    public void assertBuildOfAllShardingTables() throws SQLException {
        Collection<ShardingSphereRule> rules = Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule());
        Collection<String> tableNames = rules.stream().filter(rule -> rule instanceof TableContainedRule)
                .flatMap(shardingSphereRule -> ((TableContainedRule) shardingSphereRule).getTables().stream()).collect(Collectors.toSet());
        Collection<TableMetaData> tableMetaDatas = TableMetaDataBuilder.load(tableNames, new SchemaBuilderMaterials(
                databaseType, Collections.singletonMap("logic_db", dataSource), rules, props)).values();
        ShardingSphereSchema schemaForKernel = SchemaBuilder.buildKernelSchema(tableMetaDatas, rules);
        ShardingSphereSchema schemaForFederate = SchemaBuilder.buildFederateSchema(tableMetaDatas, rules);
        assertThat(schemaForKernel.getTables().keySet().size(), is(2));
        assertSchemaOfShardingTables(schemaForKernel.getTables().values());
        assertThat(schemaForFederate.getTables().keySet().size(), is(2));
        assertSchemaOfShardingTables(schemaForFederate.getTables().values());
    }
    
    private void assertSchemaOfShardingTables(final Collection<TableMetaData> actual) {
        Map<String, TableMetaData> tableMetaDataMap = actual.stream().collect(Collectors.toMap(TableMetaData::getName, v -> v));
        assertTrue(tableMetaDataMap.containsKey("data_node_routed_table1"));
        assertTrue(tableMetaDataMap.get("data_node_routed_table1").getColumns().isEmpty());
        assertTrue(tableMetaDataMap.containsKey("data_node_routed_table2"));
        assertTrue(tableMetaDataMap.get("data_node_routed_table2").getColumns().isEmpty());
    }
}
