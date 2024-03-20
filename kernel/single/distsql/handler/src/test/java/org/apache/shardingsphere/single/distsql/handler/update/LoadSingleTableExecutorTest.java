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

import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadSingleTableExecutorTest {
    
    private final LoadSingleTableExecutor executor = new LoadSingleTableExecutor();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(database.getSchema("foo_db")).thenReturn(schema);
    }
    
    @Test
    void assertCheckWithDuplicatedTables() {
        when(database.getName()).thenReturn("foo_db");
        when(schema.containsTable("foo")).thenReturn(true);
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "foo")));
        executor.setDatabase(database);
        assertThrows(TableExistsException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckWithInvalidStorageUnit() {
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "foo")));
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuild() {
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "foo")));
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration());
        executor.setRule(rule);
        SingleRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(actual.getTables().iterator().next(), is("ds_0.foo"));
    }
    
    @Test
    void assertUpdate() {
        Collection<String> currentTables = new LinkedList<>(Collections.singletonList("ds_0.foo"));
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration(currentTables, null);
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("ds_0", null, "bar")));
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        SingleRuleConfiguration toBeCreatedConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        Iterator<String> iterator = toBeCreatedConfig.getTables().iterator();
        assertThat(iterator.next(), is("ds_0.foo"));
        assertThat(iterator.next(), is("ds_0.bar"));
    }
}
