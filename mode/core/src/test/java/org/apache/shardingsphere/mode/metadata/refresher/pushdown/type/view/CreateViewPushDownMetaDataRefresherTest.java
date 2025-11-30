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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.view;

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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TableRefreshUtils.class, GenericSchemaBuilder.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateViewPushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final CreateViewPushDownMetaDataRefresher refresher = (CreateViewPushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, CreateViewStatement.class);
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute;
    
    @Test
    void assertRefreshCreateViewWithSingleTableMapping() throws Exception {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))));
        when(TableRefreshUtils.getTableName(sqlStatement.getView().getTableName().getIdentifier(), databaseType)).thenReturn("foo_view");
        when(TableRefreshUtils.isSingleTable("foo_view", database)).thenReturn(true);
        ShardingSphereTable loadedTable = new ShardingSphereTable("foo_view", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap("foo_schema", new ShardingSphereSchema("foo_schema", Collections.singleton(loadedTable), Collections.emptyList()));
        when(GenericSchemaBuilder.build(eq(Collections.singletonList("foo_view")), eq(database.getProtocolType()), any())).thenReturn(schemas);
        refresher.refresh(metaDataManagerPersistService, database, "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        verify(mutableDataNodeRuleAttribute).put("logic_ds", "foo_schema", "foo_view");
        verify(metaDataManagerPersistService).alterTables(database, "foo_schema", Collections.singleton(loadedTable));
    }
    
    @Test
    void assertRefreshCreateViewWithoutSingleTableMapping() throws Exception {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))));
        when(TableRefreshUtils.getTableName(sqlStatement.getView().getTableName().getIdentifier(), databaseType)).thenReturn("foo_view");
        ShardingSphereTable loadedTable = new ShardingSphereTable("foo_view", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap("foo_schema", new ShardingSphereSchema("foo_schema", Collections.singleton(loadedTable), Collections.emptyList()));
        when(GenericSchemaBuilder.build(eq(Collections.singletonList("foo_view")), eq(database.getProtocolType()), any())).thenReturn(schemas);
        refresher.refresh(metaDataManagerPersistService, database, "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        verify(mutableDataNodeRuleAttribute, never()).put("logic_ds", "foo_schema", "foo_view");
        verify(metaDataManagerPersistService).alterTables(database, "foo_schema", Collections.singleton(loadedTable));
    }
}
