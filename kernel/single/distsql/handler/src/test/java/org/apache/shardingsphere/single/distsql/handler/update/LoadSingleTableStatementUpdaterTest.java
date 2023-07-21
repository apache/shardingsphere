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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadSingleTableStatementUpdaterTest {
    
    private ShardingSphereDatabase database;
    
    private ShardingSphereSchema schema;
    
    private final LoadSingleTableStatementUpdater updater = new LoadSingleTableStatementUpdater();
    
    @BeforeEach
    void setUp() {
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.emptyList());
        when(database.getProtocolType()).thenReturn(mock(MySQLDatabaseType.class));
        schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("foo_db")).thenReturn(schema);
    }
    
    @Test
    void assertCheckWithDuplicatedTables() {
        when(database.getName()).thenReturn("foo_db");
        when(schema.containsTable("foo")).thenReturn(true);
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "foo")));
        assertThrows(TableExistsException.class, () -> updater.checkSQLStatement(database, sqlStatement, mock(SingleRuleConfiguration.class)));
    }
    
    @Test
    void assertCheckWithInvalidStorageUnit() {
        when(database.getName()).thenReturn("foo_db");
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "foo")));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> updater.checkSQLStatement(database, sqlStatement, mock(SingleRuleConfiguration.class)));
    }
    
    @Test
    void assertBuild() {
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "foo")));
        SingleRuleConfiguration actual = updater.buildToBeCreatedRuleConfiguration(mock(SingleRuleConfiguration.class), sqlStatement);
        assertThat(actual.getTables().iterator().next(), is("ds_0.foo"));
    }
    
    @Test
    void assertUpdate() {
        Collection<String> currentTables = new LinkedList<>(Collections.singletonList("ds_0.foo"));
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration(currentTables, null);
        SingleRuleConfiguration toBeCreatedRuleConfig = new SingleRuleConfiguration(Collections.singletonList("ds_0.bar"), null);
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        Iterator<String> iterator = currentConfig.getTables().iterator();
        assertThat(iterator.next(), is("ds_0.foo"));
        assertThat(iterator.next(), is("ds_0.bar"));
    }
}
