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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import org.apache.shardingsphere.distsql.statement.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowStorageUnitExecutorTest {
    
    private final ShowStorageUnitExecutor executor = new ShowStorageUnitExecutor();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getResourceMetaData()).thenReturn(new ResourceMetaData(Stream.of("ds_0", "ds_1", "ds_2").collect(Collectors.toMap(each -> each, this::createDataSource))));
        executor.setDatabase(database);
    }
    
    private MockedDataSource createDataSource(final String dataSourceName) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl("jdbc:mysql://localhost:3307/" + dataSourceName);
        result.setUsername("root");
        result.setPassword("root");
        result.setMaxPoolSize(100);
        result.setMinPoolSize(10);
        return result;
    }
    
    @Test
    void assertGetRowsWithAllStorageUnits() {
        Map<Integer, String> nameMap = new HashMap<>(3, 1F);
        nameMap.put(0, "ds_2");
        nameMap.put(1, "ds_1");
        nameMap.put(2, "ds_0");
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowStorageUnitsStatement(mock(DatabaseSegment.class), null), mock(ContextManager.class));
        assertThat(actual.size(), is(3));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        int index = 0;
        while (rowData.hasNext()) {
            LocalDataQueryResultRow data = rowData.next();
            assertThat(data.getCell(1), is(nameMap.get(index)));
            assertThat(data.getCell(2), is("MySQL"));
            assertThat(data.getCell(3), is("localhost"));
            assertThat(data.getCell(4), is("3307"));
            assertThat(data.getCell(5), is(nameMap.get(index)));
            assertThat(data.getCell(6), is(""));
            assertThat(data.getCell(7), is(""));
            assertThat(data.getCell(8), is(""));
            assertThat(data.getCell(9), is("100"));
            assertThat(data.getCell(10), is("10"));
            assertThat(data.getCell(11), is(""));
            assertThat(data.getCell(12), is("{\"openedConnections\":[],\"closed\":false}"));
            index++;
        }
    }
    
    @Test
    void assertGetRowsWithUnusedStorageUnits() {
        RuleMetaData metaData = mockUnusedStorageUnitsRuleMetaData();
        when(database.getRuleMetaData()).thenReturn(metaData);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowStorageUnitsStatement(mock(DatabaseSegment.class), 0), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        LocalDataQueryResultRow data = rowData.next();
        assertThat(data.getCell(1), is("ds_2"));
        assertThat(data.getCell(2), is("MySQL"));
        assertThat(data.getCell(3), is("localhost"));
        assertThat(data.getCell(4), is("3307"));
        assertThat(data.getCell(5), is("ds_2"));
        assertThat(data.getCell(6), is(""));
        assertThat(data.getCell(7), is(""));
        assertThat(data.getCell(8), is(""));
        assertThat(data.getCell(9), is("100"));
        assertThat(data.getCell(10), is("10"));
        assertThat(data.getCell(11), is(""));
        assertThat(data.getCell(12), is("{\"openedConnections\":[],\"closed\":false}"));
    }
    
    private RuleMetaData mockUnusedStorageUnitsRuleMetaData() {
        RuleMetaData result = mock(RuleMetaData.class);
        Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnitNameAndRulesMap = new HashMap<>(2, 1F);
        inUsedStorageUnitNameAndRulesMap.put("ds_0", Collections.singleton(ShardingSphereRule.class));
        inUsedStorageUnitNameAndRulesMap.put("ds_1", Collections.singleton(ShardingSphereRule.class));
        when(result.getInUsedStorageUnitNameAndRulesMap()).thenReturn(inUsedStorageUnitNameAndRulesMap);
        return result;
    }
}
