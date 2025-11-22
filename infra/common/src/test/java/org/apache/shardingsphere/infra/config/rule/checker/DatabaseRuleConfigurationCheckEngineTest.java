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

package org.apache.shardingsphere.infra.config.rule.checker;

import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(OrderedSPILoader.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseRuleConfigurationCheckEngineTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private RuleMetaData ruleMetaData;
    
    @Mock
    private FixtureRuleConfiguration ruleConfig;
    
    @Mock
    private DatabaseRuleConfigurationChecker<FixtureRuleConfiguration> checker;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("foo_db");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    void assertCheckWithoutChecker() {
        when(OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(FixtureRuleConfiguration.class))).thenReturn(Collections.emptyMap());
        assertDoesNotThrow(() -> DatabaseRuleConfigurationCheckEngine.check(new FixtureRuleConfiguration(), database));
    }
    
    @Test
    void assertCheckWithEmptyDataSourcesAndTables() {
        when(OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(FixtureRuleConfiguration.class)))
                .thenReturn(Collections.singletonMap(FixtureRuleConfiguration.class, checker));
        assertDoesNotThrow(() -> DatabaseRuleConfigurationCheckEngine.check(ruleConfig, database));
    }
    
    @Test
    void assertCheckWithMissingDataSourcesButInLogicDataSources() {
        when(OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(FixtureRuleConfiguration.class)))
                .thenReturn(Collections.singletonMap(FixtureRuleConfiguration.class, checker));
        Collection<String> requiredDataSources = Arrays.asList("foo_ds", "bar_ds");
        when(checker.getRequiredDataSourceNames(any())).thenReturn(requiredDataSources);
        when(resourceMetaData.getNotExistedDataSources(requiredDataSources)).thenReturn(new LinkedList<>(Collections.singleton("foo_ds")));
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("bar_ds", mock(StorageUnit.class, RETURNS_DEEP_STUBS)));
        DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(dataSourceMapperRuleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton("some_logic_name")));
        when(ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.singleton(dataSourceMapperRuleAttribute));
        assertDoesNotThrow(() -> DatabaseRuleConfigurationCheckEngine.check(ruleConfig, database));
    }
    
    @Test
    void assertCheckWithDuplicatedTableNames() {
        when(OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(FixtureRuleConfiguration.class)))
                .thenReturn(Collections.singletonMap(FixtureRuleConfiguration.class, checker));
        when(checker.getTableNames(any())).thenReturn(Arrays.asList("foo_tbl", "bar_tbl", "foo_tbl"));
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        DuplicateRuleException exception = assertThrows(DuplicateRuleException.class, () -> DatabaseRuleConfigurationCheckEngine.check(ruleConfig, database));
        assertThat(exception.getMessage(), is("Duplicate Fixture rule names 'foo_tbl' in database 'foo_db'."));
    }
}
