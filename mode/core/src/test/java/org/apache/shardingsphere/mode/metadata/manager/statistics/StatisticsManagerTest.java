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

package org.apache.shardingsphere.mode.metadata.manager.statistics;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StatisticsManagerTest {
    
    private static final String DATABASE = "foo_db";
    
    private static final String SCHEMA = "foo_schema";
    
    private static final String TABLE = "foo_tbl";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertAddDatabaseStatisticsWhenAbsent() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addDatabaseStatistics(DATABASE);
        assertTrue(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertAddDatabaseStatisticsWhenExists() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        DatabaseStatistics expected = new DatabaseStatistics();
        statistics.putDatabaseStatistics(DATABASE, expected);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addDatabaseStatistics(DATABASE);
        assertThat(statistics.getDatabaseStatistics(DATABASE), is(expected));
    }
    
    @Test
    void assertDropDatabaseStatisticsWhenAbsent() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropDatabaseStatistics(DATABASE);
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertDropDatabaseStatisticsWhenExists() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        statistics.putDatabaseStatistics(DATABASE, new DatabaseStatistics());
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropDatabaseStatistics(DATABASE);
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertAddSchemaStatisticsWhenSchemaExists() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        SchemaStatistics expected = new SchemaStatistics();
        databaseStatistics.putSchemaStatistics(SCHEMA, expected);
        statistics.putDatabaseStatistics(DATABASE, databaseStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addSchemaStatistics(DATABASE, SCHEMA);
        assertThat(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA), is(expected));
    }
    
    @Test
    void assertAddSchemaStatisticsWhenSchemaAbsent() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        statistics.putDatabaseStatistics(DATABASE, new DatabaseStatistics());
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addSchemaStatistics(DATABASE, SCHEMA);
        assertTrue(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertDropSchemaStatisticsWhenSchemaMissing() {
        ShardingSphereStatistics statistics = createStatisticsWithDatabaseOnly();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropSchemaStatistics(DATABASE, SCHEMA);
        assertFalse(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertDropSchemaStatisticsWhenDatabaseNotFound() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropSchemaStatistics(DATABASE, SCHEMA);
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertDropSchemaStatisticsWhenSchemaExists() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        databaseStatistics.putSchemaStatistics(SCHEMA, new SchemaStatistics());
        statistics.putDatabaseStatistics(DATABASE, databaseStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropSchemaStatistics(DATABASE, SCHEMA);
        assertFalse(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertAddTableStatisticsWhenSchemaMissing() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addTableStatistics(DATABASE, SCHEMA, TABLE);
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertAddTableStatisticsWhenTableExists() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        TableStatistics expected = new TableStatistics(TABLE);
        schemaStatistics.putTableStatistics(TABLE, expected);
        databaseStatistics.putSchemaStatistics(SCHEMA, schemaStatistics);
        statistics.putDatabaseStatistics(DATABASE, databaseStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addTableStatistics(DATABASE, SCHEMA, TABLE);
        assertThat(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).getTableStatistics(TABLE), is(expected));
    }
    
    @Test
    void assertAddTableStatisticsWhenTableAbsent() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        databaseStatistics.putSchemaStatistics(SCHEMA, new SchemaStatistics());
        statistics.putDatabaseStatistics(DATABASE, databaseStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addTableStatistics(DATABASE, SCHEMA, TABLE);
        assertTrue(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).containsTableStatistics(TABLE));
    }
    
    @Test
    void assertAddTableStatisticsWhenSchemaMissingButDatabaseExists() {
        ShardingSphereStatistics statistics = createStatisticsWithDatabaseOnly();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).addTableStatistics(DATABASE, SCHEMA, TABLE);
        assertFalse(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertDropTableStatisticsWhenDatabaseMissing() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropTableStatistics(DATABASE, SCHEMA, TABLE);
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertDropTableStatisticsWhenSchemaMissing() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        statistics.putDatabaseStatistics(DATABASE, new DatabaseStatistics());
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropTableStatistics(DATABASE, SCHEMA, TABLE);
        assertFalse(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertDropTableStatisticsWhenTableExists() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics(TABLE, new TableStatistics(TABLE));
        databaseStatistics.putSchemaStatistics(SCHEMA, schemaStatistics);
        statistics.putDatabaseStatistics(DATABASE, databaseStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).dropTableStatistics(DATABASE, SCHEMA, TABLE);
        assertFalse(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).containsTableStatistics(TABLE));
    }
    
    @Test
    void assertAlterRowStatisticsWhenStatisticsMissing() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, new YamlRowStatistics());
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertAlterRowStatisticsWhenMetaDataMissing() {
        ShardingSphereStatistics statistics = createStatisticsWithTable();
        MetaDataContexts metaDataContexts = new MetaDataContexts(new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())), statistics);
        YamlRowStatistics yamlRowStatistics = new YamlRowStatistics();
        yamlRowStatistics.setUniqueKey("uk_1");
        yamlRowStatistics.setRows(Collections.singletonList("1"));
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, yamlRowStatistics);
        assertTrue(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).getTableStatistics(TABLE).getRows().isEmpty());
    }
    
    @Test
    void assertAlterRowStatisticsWhenSchemaMissingInStatistics() {
        ShardingSphereStatistics statistics = createStatisticsWithDatabaseOnly();
        ShardingSphereMetaData metaData = createMetaDataWithTable();
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, new YamlRowStatistics());
        assertFalse(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertAlterRowStatisticsWhenTableMissingInStatistics() {
        ShardingSphereStatistics statistics = createStatisticsWithSchemaWithoutTable();
        ShardingSphereMetaData metaData = createMetaDataWithTable();
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, new YamlRowStatistics());
        assertFalse(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).containsTableStatistics(TABLE));
    }
    
    @Test
    void assertAlterRowStatisticsWhenSchemaMissingInMetaData() {
        ShardingSphereStatistics statistics = createStatisticsWithTable();
        ShardingSphereMetaData metaData = createMetaDataWithDatabaseOnly();
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, new YamlRowStatistics());
        TableStatistics tableStatistics = statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).getTableStatistics(TABLE);
        assertTrue(tableStatistics.getRows().isEmpty());
    }
    
    @Test
    void assertAlterRowStatisticsWhenTableMissingInMetaData() {
        ShardingSphereStatistics statistics = createStatisticsWithTable();
        ShardingSphereMetaData metaData = createMetaDataWithSchemaWithoutTable();
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, new YamlRowStatistics());
        TableStatistics tableStatistics = statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).getTableStatistics(TABLE);
        assertTrue(tableStatistics.getRows().isEmpty());
    }
    
    @Test
    void assertAlterRowStatisticsSuccess() {
        ShardingSphereStatistics statistics = createStatisticsWithTable();
        ShardingSphereMetaData metaData = createMetaDataWithTable();
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        YamlRowStatistics yamlRowStatistics = new YamlRowStatistics();
        yamlRowStatistics.setUniqueKey("uk_success");
        yamlRowStatistics.setRows(Arrays.asList("1", "2"));
        new StatisticsManager(metaDataContexts).alterRowStatistics(DATABASE, SCHEMA, TABLE, yamlRowStatistics);
        TableStatistics tableStatistics = statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).getTableStatistics(TABLE);
        assertThat(tableStatistics.getRows().iterator().next().getUniqueKey(), is("uk_success"));
    }
    
    @Test
    void assertDeleteRowStatisticsWhenMissing() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).deleteRowStatistics(DATABASE, SCHEMA, TABLE, "uk");
        assertFalse(statistics.containsDatabaseStatistics(DATABASE));
    }
    
    @Test
    void assertDeleteRowStatisticsWhenSchemaMissing() {
        ShardingSphereStatistics statistics = createStatisticsWithDatabaseOnly();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).deleteRowStatistics(DATABASE, SCHEMA, TABLE, "uk");
        assertFalse(statistics.getDatabaseStatistics(DATABASE).containsSchemaStatistics(SCHEMA));
    }
    
    @Test
    void assertDeleteRowStatisticsWhenTableMissing() {
        ShardingSphereStatistics statistics = createStatisticsWithSchemaWithoutTable();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).deleteRowStatistics(DATABASE, SCHEMA, TABLE, "uk");
        assertFalse(statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).containsTableStatistics(TABLE));
    }
    
    @Test
    void assertDeleteRowStatisticsSuccess() {
        ShardingSphereStatistics statistics = createStatisticsWithTable();
        TableStatistics tableStatistics = statistics.getDatabaseStatistics(DATABASE).getSchemaStatistics(SCHEMA).getTableStatistics(TABLE);
        RowStatistics rowStatistics = new RowStatistics("uk_to_delete", Collections.singletonList("foo"));
        tableStatistics.getRows().add(rowStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(ShardingSphereMetaData.class), statistics);
        new StatisticsManager(metaDataContexts).deleteRowStatistics(DATABASE, SCHEMA, TABLE, "uk_to_delete");
        assertTrue(tableStatistics.getRows().isEmpty());
    }
    
    private ShardingSphereStatistics createStatisticsWithTable() {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics(TABLE, new TableStatistics(TABLE));
        databaseStatistics.putSchemaStatistics(SCHEMA, schemaStatistics);
        result.putDatabaseStatistics(DATABASE, databaseStatistics);
        return result;
    }
    
    private ShardingSphereStatistics createStatisticsWithDatabaseOnly() {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        result.putDatabaseStatistics(DATABASE, new DatabaseStatistics());
        return result;
    }
    
    private ShardingSphereStatistics createStatisticsWithSchemaWithoutTable() {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        databaseStatistics.putSchemaStatistics(SCHEMA, new SchemaStatistics());
        result.putDatabaseStatistics(DATABASE, databaseStatistics);
        return result;
    }
    
    private ShardingSphereMetaData createMetaDataWithTable() {
        ShardingSphereTable table = new ShardingSphereTable(TABLE, Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(SCHEMA, databaseType, Collections.singleton(table), Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                DATABASE, databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
        return new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereMetaData createMetaDataWithDatabaseOnly() {
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                DATABASE, databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        return new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereMetaData createMetaDataWithSchemaWithoutTable() {
        ShardingSphereSchema schema = new ShardingSphereSchema(SCHEMA, mock(DatabaseType.class));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                DATABASE, databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
        return new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
}
