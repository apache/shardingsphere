/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.config.listener;

import io.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceChangedListenerTest {
    
    private static final String DATA_SOURCE_YAML = "master_ds: !!io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n" + "  properties:\n"
            + "    url: jdbc:mysql://localhost:3306/demo_ds_master\n" + "    username: root\n" + "    password: null\n";
    
    private DataSourceChangedListener dataSourceChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Before
    public void setUp() {
        dataSourceChangedListener = new DataSourceChangedListener("test", regCenter, "sharding_db");
    }
    
    @Test
    public void assertCreateShardingOrchestrationEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("test", DATA_SOURCE_YAML, ChangedType.UPDATED);
        DataSourceChangedEvent actual = dataSourceChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual.getShardingSchemaName(), is("sharding_db"));
        assertThat(actual.getDataSourceConfigurations().size(), is(1));
        assertThat(actual.getDataSourceConfigurations().get("master_ds").getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
    }
}
