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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.table;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TableRefreshUtils.class, GenericSchemaBuilder.class})
class AlterTablePushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final AlterTablePushDownMetaDataRefresher refresher = (AlterTablePushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, AlterTableStatement.class);
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute;
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshRenameTable() throws Exception {
        ShardingSphereTable renamedTable = new ShardingSphereTable("bar_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap("foo_schema", new ShardingSphereSchema("foo_schema", Collections.singleton(renamedTable), Collections.emptyList()));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        AlterTableStatement sqlStatement = new AlterTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        sqlStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_tbl"))));
        when(TableRefreshUtils.getTableName(sqlStatement.getTable().getTableName().getIdentifier(), databaseType)).thenReturn("foo_tbl");
        when(TableRefreshUtils.isSingleTable(any(), any())).thenReturn(true);
        when(GenericSchemaBuilder.build(eq(Collections.singletonList("bar_tbl")), eq(database.getProtocolType()), any())).thenReturn(schemas);
        refresher.refresh(metaDataManagerPersistService, database, "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        ArgumentCaptor<Collection<ShardingSphereTable>> alteredTablesCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<String>> droppedTablesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(mutableDataNodeRuleAttribute).put("logic_ds", "foo_schema", "bar_tbl");
        verify(metaDataManagerPersistService).alterTables(eq(database), eq("foo_schema"), alteredTablesCaptor.capture());
        verify(metaDataManagerPersistService).dropTables(eq(database), eq("foo_schema"), droppedTablesCaptor.capture());
        assertThat(alteredTablesCaptor.getValue().iterator().next().getName(), is("bar_tbl"));
        assertThat(droppedTablesCaptor.getValue().iterator().next(), is("foo_tbl"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshAlterTableWithoutRename() throws Exception {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap("foo_schema", new ShardingSphereSchema("foo_schema", Collections.singleton(table), Collections.emptyList()));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        AlterTableStatement sqlStatement = new AlterTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        when(TableRefreshUtils.getTableName(sqlStatement.getTable().getTableName().getIdentifier(), databaseType)).thenReturn("foo_tbl");
        when(TableRefreshUtils.isSingleTable("foo_tbl", database)).thenReturn(false);
        when(GenericSchemaBuilder.build(eq(Collections.singletonList("foo_tbl")), eq(database.getProtocolType()), any())).thenReturn(schemas);
        refresher.refresh(metaDataManagerPersistService, database, "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        ArgumentCaptor<Collection<ShardingSphereTable>> alteredTablesCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<String>> droppedTablesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(metaDataManagerPersistService).alterTables(eq(database), eq("foo_schema"), alteredTablesCaptor.capture());
        verify(metaDataManagerPersistService).dropTables(eq(database), eq("foo_schema"), droppedTablesCaptor.capture());
        assertThat(alteredTablesCaptor.getValue().iterator().next().getName(), is("foo_tbl"));
        assertTrue(droppedTablesCaptor.getValue().isEmpty());
    }
}
