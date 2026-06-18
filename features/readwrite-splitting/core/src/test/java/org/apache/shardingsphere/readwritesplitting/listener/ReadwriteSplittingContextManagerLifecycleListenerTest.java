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

package org.apache.shardingsphere.readwritesplitting.listener;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadwriteSplittingContextManagerLifecycleListenerTest {
    
    private final ReadwriteSplittingContextManagerLifecycleListener listener = new ReadwriteSplittingContextManagerLifecycleListener();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("onInitializedArguments")
    void assertOnInitialized(final String name, final Collection<String> databaseNames, final Map<String, DataSourceState> states,
                             final List<String> expectedQualifiedDataSources, final List<DataSourceState> expectedStates, final int expectedUpdateCount) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        StaticDataSourceRuleAttribute attribute = mock(StaticDataSourceRuleAttribute.class);
        Map<String, QualifiedDataSourceState> qualifiedDataSourceStates = states.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new QualifiedDataSourceState(entry.getValue())));
        Collection<ShardingSphereDatabase> databases = createDatabases(databaseNames, attribute);
        when(contextManager.getPersistServiceFacade().getQualifiedDataSourceStateService().load()).thenReturn(qualifiedDataSourceStates);
        when(contextManager.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(databases);
        listener.onInitialized(contextManager);
        ArgumentCaptor<QualifiedDataSource> qualifiedDataSourceCaptor = ArgumentCaptor.forClass(QualifiedDataSource.class);
        ArgumentCaptor<DataSourceState> stateCaptor = ArgumentCaptor.forClass(DataSourceState.class);
        verify(attribute, times(expectedUpdateCount)).updateStatus(qualifiedDataSourceCaptor.capture(), stateCaptor.capture());
        assertThat(qualifiedDataSourceCaptor.getAllValues().stream().map(QualifiedDataSource::toString).collect(Collectors.toList()), CoreMatchers.is(expectedQualifiedDataSources));
        assertThat(stateCaptor.getAllValues(), CoreMatchers.is(expectedStates));
    }
    
    private Collection<ShardingSphereDatabase> createDatabases(final Collection<String> databaseNames, final StaticDataSourceRuleAttribute attribute) {
        Collection<ShardingSphereDatabase> result = new LinkedList<>();
        for (String each : databaseNames) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
            when(database.getName()).thenReturn(each);
            RuleMetaData ruleMetaData = mock(RuleMetaData.class);
            when(ruleMetaData.getAttributes(StaticDataSourceRuleAttribute.class)).thenReturn(Collections.singletonList(attribute));
            when(database.getRuleMetaData()).thenReturn(ruleMetaData);
            result.add(database);
        }
        return result;
    }
    
    @Test
    void assertOnDestroyed() {
        assertDoesNotThrow(() -> listener.onDestroyed(mock(ContextManager.class)));
    }
    
    private static Stream<Arguments> onInitializedArguments() {
        return Stream.of(
                Arguments.of("matched database", Collections.singletonList("foo_db"), Collections.singletonMap("foo_db.group_0.ds_0", DataSourceState.DISABLED),
                        Collections.singletonList("foo_db.group_0.ds_0"), Collections.singletonList(DataSourceState.DISABLED), 1),
                Arguments.of("mismatched database", Collections.singletonList("bar_db"), Collections.singletonMap("foo_db.group_0.ds_0", DataSourceState.DISABLED),
                        Collections.emptyList(), Collections.emptyList(), 0),
                Arguments.of("matched and mismatched databases", Arrays.asList("foo_db", "bar_db"), createDataSourceStates(),
                        Arrays.asList("foo_db.group_0.ds_0", "foo_db.group_0.ds_1"), Arrays.asList(DataSourceState.DISABLED, DataSourceState.ENABLED), 2));
    }
    
    private static Map<String, DataSourceState> createDataSourceStates() {
        Map<String, DataSourceState> result = new LinkedHashMap<>(2, 1F);
        result.put("foo_db.group_0.ds_0", DataSourceState.DISABLED);
        result.put("foo_db.group_0.ds_1", DataSourceState.ENABLED);
        return result;
    }
}
