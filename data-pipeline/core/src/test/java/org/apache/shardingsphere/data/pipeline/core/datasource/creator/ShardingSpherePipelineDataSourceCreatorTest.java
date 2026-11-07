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

package org.apache.shardingsphere.data.pipeline.core.datasource.creator;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DataSourcePoolCreator.class, ShardingSphereDataSourceFactory.class, PipelineContextManager.class})
class ShardingSpherePipelineDataSourceCreatorTest {
    
    @Test
    void assertCreateWithProjectConfigurations() throws SQLException {
        ShardingSpherePipelineDataSourceConfiguration config = mockConfiguration();
        DataSource expected = new MockedDataSource();
        Map<String, DataSource> dataSources = Collections.singletonMap("ds_0", new MockedDataSource());
        when(DataSourcePoolCreator.create(config.getDataSourcePoolPropertiesMap(), false)).thenReturn(dataSources);
        AtomicReference<ModeConfiguration> actualMode = new AtomicReference<>();
        AtomicReference<Collection<RuleConfiguration>> actualRules = new AtomicReference<>();
        AtomicReference<Properties> actualProps = new AtomicReference<>();
        when(ShardingSphereDataSourceFactory.createDataSource(eq("foo_db"), any(ModeConfiguration.class), eq(dataSources), anyCollection(), any(Properties.class)))
                .thenAnswer(invocation -> {
                    actualMode.set(invocation.getArgument(1));
                    actualRules.set(invocation.getArgument(3));
                    actualProps.set(invocation.getArgument(4));
                    return expected;
                });
        DataSource actual = new ShardingSpherePipelineDataSourceCreator().create(config);
        assertThat(actual, sameInstance(expected));
        assertThat(actualMode.get().getType(), is("Standalone"));
        assertThat(actualMode.get().getRepository().getType(), is("Memory"));
        assertThat(actualRules.get().size(), is(1));
        assertTrue(actualRules.get().stream().noneMatch(AuthorityRuleConfiguration.class::isInstance));
        assertThat(actualProps.get().get("max-connections-size-per-query"), is(100000));
    }
    
    @Test
    void assertCreateCleansUpDataSourcesOnFailure() throws SQLException {
        ShardingSpherePipelineDataSourceConfiguration config = mockConfiguration();
        Map<String, DataSource> dataSources = Collections.singletonMap("ds_0", new MockedDataSource());
        when(DataSourcePoolCreator.create(config.getDataSourcePoolPropertiesMap(), false)).thenReturn(dataSources);
        SQLException expected = new SQLException("fixture_failure");
        when(ShardingSphereDataSourceFactory.createDataSource(eq("foo_db"), any(ModeConfiguration.class), eq(dataSources), anyCollection(), any(Properties.class)))
                .thenThrow(expected);
        try (MockedConstruction<DataSourcePoolDestroyer> destroyers = mockConstruction(DataSourcePoolDestroyer.class)) {
            assertThat(assertThrows(SQLException.class, () -> new ShardingSpherePipelineDataSourceCreator().create(config)), sameInstance(expected));
            assertThat(destroyers.constructed().size(), is(1));
            verify(destroyers.constructed().get(0)).asyncDestroy();
        }
    }
    
    private ShardingSpherePipelineDataSourceConfiguration mockConfiguration() {
        ShardingSpherePipelineDataSourceConfiguration result = mock(ShardingSpherePipelineDataSourceConfiguration.class);
        when(result.getDatabaseName()).thenReturn("foo_db");
        when(result.getDataSourcePoolPropertiesMap()).thenReturn(Collections.singletonMap("ds_0", mock(DataSourcePoolProperties.class)));
        Collection<RuleConfiguration> rules = new LinkedList<>();
        rules.add(mock(AuthorityRuleConfiguration.class));
        rules.add(mock(RuleConfiguration.class));
        when(result.getCreationRuleConfigurations()).thenReturn(rules);
        return result;
    }
}
