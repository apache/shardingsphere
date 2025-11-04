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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SchemaMetaDataReviseEngineTest {
    
    @Test
    void assertReviseWithoutMetaDataReviseEntry() {
        SchemaMetaData schemaMetaData = new SchemaMetaData("foo_schema", Collections.singleton(mock(TableMetaData.class)));
        SchemaMetaData actual = new SchemaMetaDataReviseEngine(Collections.emptyList(), new ConfigurationProperties(new Properties())).revise(schemaMetaData);
        assertThat(actual.getName(), is(schemaMetaData.getName()));
        assertThat(actual.getTables(), is(schemaMetaData.getTables()));
    }
    
    @Test
    void assertReviseWithMetaDataReviseEntry() {
        SchemaMetaData schemaMetaData = new SchemaMetaData("foo_schema", Collections.singletonList(createTableMetaData()));
        SchemaMetaData actual = new SchemaMetaDataReviseEngine(Collections.singleton(new FixtureGlobalRule()), new ConfigurationProperties(new Properties())).revise(schemaMetaData);
        assertThat(actual.getName(), is(schemaMetaData.getName()));
        assertThat(actual.getTables().size(), is(schemaMetaData.getTables().size()));
        Iterator<TableMetaData> expectedTableIterator = schemaMetaData.getTables().iterator();
        actual.getTables().forEach(each -> assertTableMetaData(each, expectedTableIterator.next()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertReviseWithoutAggregationReviser() {
        SchemaMetaData schemaMetaData = new SchemaMetaData("foo_schema", Collections.singleton(createTableMetaData()));
        MetaDataReviseEntry<FixtureGlobalRule> reviseEntry = mock(MetaDataReviseEntry.class);
        when(reviseEntry.getTypeClass()).thenReturn(FixtureGlobalRule.class);
        ShardingSphereRule rule = new FixtureGlobalRule();
        Map<ShardingSphereRule, MetaDataReviseEntry<?>> entries = Collections.singletonMap(rule, reviseEntry);
        try (MockedStatic<OrderedSPILoader> mocked = mockStatic(OrderedSPILoader.class)) {
            mocked.when(() -> OrderedSPILoader.getServices(MetaDataReviseEntry.class, Collections.singleton(rule))).thenReturn(entries);
            SchemaMetaData actual = new SchemaMetaDataReviseEngine(Collections.singleton(rule), new ConfigurationProperties(new Properties())).revise(schemaMetaData);
            assertThat(actual.getName(), is(schemaMetaData.getName()));
            assertThat(actual.getTables().size(), is(schemaMetaData.getTables().size()));
        }
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = new LinkedHashSet<>(Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false, false),
                new ColumnMetaData("pwd_cipher", Types.VARCHAR, false, false, true, true, false, false),
                new ColumnMetaData("pwd_like", Types.VARCHAR, false, false, true, true, false, false)));
        IndexMetaData indexMetaData = new IndexMetaData("index_name");
        ConstraintMetaData constraintMetaData = new ConstraintMetaData("constraint_name", "table_name_2");
        return new TableMetaData("table_name", columns, Collections.singletonList(indexMetaData), Collections.singleton(constraintMetaData));
    }
    
    private void assertTableMetaData(final TableMetaData actual, final TableMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getColumns().size(), is(expected.getColumns().size()));
        assertThat(actual.getIndexes().size(), is(expected.getIndexes().size()));
        assertThat(actual.getConstraints().size(), is(expected.getConstraints().size()));
        assertThat(actual.getType(), is(expected.getType()));
        Iterator<ColumnMetaData> expectedColumnIterator = expected.getColumns().iterator();
        actual.getColumns().forEach(each -> assertColumnMetaData(each, expectedColumnIterator.next()));
        Iterator<IndexMetaData> expectedIndexIterator = expected.getIndexes().iterator();
        actual.getIndexes().forEach(each -> assertIndexMetaData(each, expectedIndexIterator.next()));
        Iterator<ConstraintMetaData> expectedConstraintIterator = expected.getConstraints().iterator();
        actual.getConstraints().forEach(each -> assertConstraintMetaData(each, expectedConstraintIterator.next()));
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final ColumnMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getDataType(), is(expected.getDataType()));
        assertThat(actual.isPrimaryKey(), is(expected.isPrimaryKey()));
        assertThat(actual.isGenerated(), is(expected.isGenerated()));
        assertThat(actual.isCaseSensitive(), is(expected.isCaseSensitive()));
        assertThat(actual.isVisible(), is(expected.isVisible()));
        assertThat(actual.isUnsigned(), is(expected.isUnsigned()));
        assertThat(actual.isNullable(), is(expected.isNullable()));
    }
    
    private void assertIndexMetaData(final IndexMetaData actual, final IndexMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getColumns(), is(expected.getColumns()));
        assertThat(actual.isUnique(), is(expected.isUnique()));
    }
    
    private void assertConstraintMetaData(final ConstraintMetaData actual, final ConstraintMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getReferencedTableName(), is(expected.getReferencedTableName()));
    }
}
