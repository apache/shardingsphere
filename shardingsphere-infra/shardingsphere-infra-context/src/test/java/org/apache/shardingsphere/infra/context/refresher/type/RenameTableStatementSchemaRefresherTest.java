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

package org.apache.shardingsphere.infra.context.refresher.type;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RenameTableStatementSchemaRefresherTest {
    
    @Mock
    private FederationDatabaseMetaData database;
    
    @Mock
    private Map<String, OptimizerPlannerContext> optimizerPlanners;
    
    @Mock
    private RenameTableStatement sqlStatement;
    
    @InjectMocks
    private ConfigurationProperties props;
    
    @Mock
    private ShardingSphereResource shardingSphereResource;
    
    @Mock
    private ShardingSphereSchema shardingSphereSchema;
    
    private final RenameTableStatementSchemaRefresher renameTableStatementSchemaRefresher = new RenameTableStatementSchemaRefresher();
    
    @Test
    public void assertRefreshNonRenameTableStatement() {
        refreshRenameTableStatement(0);
    }
    
    @Test
    public void assertRefreshOneRenameTableStatement() {
        refreshRenameTableStatement(1);
    }
    
    @Test
    public void assertRefreshMultipleRenameTableStatement() {
        refreshRenameTableStatement(2);
    }
    
    @SneakyThrows
    private void refreshRenameTableStatement(final int renameStatementCount) {
        when(sqlStatement.getRenameTables()).thenReturn(getRenameTableDefinitionSegments(renameStatementCount));
        doNothing().when(shardingSphereSchema).remove(anyString());
        Map<String, DataSource> dataSources = new HashMap<>();
        when(shardingSphereResource.getDataSources()).thenReturn(dataSources);
        when(shardingSphereResource.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        when(database.getName()).thenReturn("DATABASE_NAME");
        Collection<String> logicDataSourceNames = new LinkedList<>();
        logicDataSourceNames.add("LOGIC_DATA_SOURCE_NAME");
        RenameTableLister listener = new RenameTableLister(renameStatementCount);
        ShardingSphereEventBus.getInstance().register(listener);
        renameTableStatementSchemaRefresher.refresh(createShardingSphereMetaData(), database, optimizerPlanners, logicDataSourceNames, sqlStatement, props);
        assertThat(listener.actualCount, is(listener.renameCount));
        ShardingSphereEventBus.getInstance().unregister(listener);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        return new ShardingSphereMetaData("SCHEMA_META_DATA_NAME", 
                shardingSphereResource, new ShardingSphereRuleMetaData(new LinkedList<>(), new LinkedList<>()), Collections.singletonMap("SCHEMA_META_DATA_NAME", shardingSphereSchema));
    }
    
    private Collection<RenameTableDefinitionSegment> getRenameTableDefinitionSegments(final int renameStatementCount) {
        Collection<RenameTableDefinitionSegment> result = new LinkedList<>();
        for (int i = 0; i < renameStatementCount; i++) {
            SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 1, new IdentifierValue("TABLE_" + i)));
            SimpleTableSegment newSimpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 1, new IdentifierValue("NEW_TABLE_" + i)));
            RenameTableDefinitionSegment renameTableDefinitionSegment = new RenameTableDefinitionSegment(0, 1);
            renameTableDefinitionSegment.setTable(simpleTableSegment);
            renameTableDefinitionSegment.setRenameTable(newSimpleTableSegment);
            result.add(renameTableDefinitionSegment);
        }
        return result;
    }
    
    @RequiredArgsConstructor
    private static final class RenameTableLister {
        
        private final int renameCount;
        
        private int actualCount = -1;
        
        @Subscribe
        public void process(final Object message) {
            if (message instanceof SchemaAlteredEvent) {
                actualCount = ((SchemaAlteredEvent) message).getAlteredTables().size();
            }
        }
    }
}
