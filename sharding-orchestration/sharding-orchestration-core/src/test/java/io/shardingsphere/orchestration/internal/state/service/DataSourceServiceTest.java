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

package io.shardingsphere.orchestration.internal.state.service;

import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceServiceTest {
    
    private static final String DATA_SOURCE_YAML =
            "ds_0: !!io.shardingsphere.core.config.DataSourceConfiguration\n"
                    + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
                    + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_0\n" + "    username: root\n" + "    password: root\n"
                    + "ds_1: !!io.shardingsphere.core.config.DataSourceConfiguration\n"
                    + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
                    + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_1\n" + "    username: root\n" + "    password: root\n"
                    + "ds_0_slave: !!io.shardingsphere.core.config.DataSourceConfiguration\n"
                    + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
                    + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_0_slave\n" + "    username: root\n" + "    password: root\n"
                    + "ds_1_slave: !!io.shardingsphere.core.config.DataSourceConfiguration\n"
                    + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
                    + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_1_slave\n" + "    username: root\n" + "    password: root\n";
    
    private static final String SHARDING_MASTER_SLAVE_RULE_YAML = "tables:\n" + "  t_order: \n" + "    actualDataNodes: db_ms_${0..1}.t_order_${0..1}\n" + "    databaseStrategy: \n"
            + "      inline:\n" + "        shardingColumn: user_id\n" + "        algorithmExpression: db_ms_${order_id % 2}\n" + "    tableStrategy: \n" + "      inline:\n"
            + "        shardingColumn: order_id\n" + "        algorithmExpression: t_order_${order_id % 2}\n" + "    keyGeneratorColumnName: order_id\n"
            + "masterSlaveRules:\n" + "  db_ms_0:\n" + "    masterDataSourceName: db_0\n" + "    slaveDataSourceNames:\n"
            + "      - db_0_slave\n" + "  db_ms_1:\n" + "    masterDataSourceName: db_1\n" + "    slaveDataSourceNames:\n" + "      - db_1_slave";
    
    @Mock
    private RegistryCenter regCenter;
    
    private DataSourceService dataSourceService;
    
    @Before
    public void setUp() {
        dataSourceService = new DataSourceService("test", regCenter);
    }
    
    @Test
    public void assertPersistDataSourcesNode() {
        dataSourceService.persistDataSourcesNode();
        verify(regCenter).persist("/test/state/datasources", "");
    }
    
    @Test
    public void assertGetAvailableDataSourceConfigurationsWithDisabledDataSources() {
        when(regCenter.getChildrenKeys("/test/config/schema")).thenReturn(Collections.singletonList("sharding_db"));
        when(regCenter.getDirectly("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_MASTER_SLAVE_RULE_YAML);
        when(regCenter.getDirectly("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(regCenter.getChildrenKeys("/test/state/datasources")).thenReturn(Collections.singletonList("sharding_db.ds_0_slave"));
        when(regCenter.get("/test/state/datasources/sharding_db.ds_0_slave")).thenReturn("disabled");
        Map<String, DataSourceConfiguration> availableDataSourceConfigs = dataSourceService.getAvailableDataSourceConfigurations("sharding_db");
        assertThat(availableDataSourceConfigs.size(), is(3));
    }
    
    @Test
    public void assertGetAvailableDataSourceConfigurationsWithoutDisabledDataSources() {
        when(regCenter.getChildrenKeys("/test/config/schema")).thenReturn(Collections.singletonList("sharding_db"));
        when(regCenter.getDirectly("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_MASTER_SLAVE_RULE_YAML);
        when(regCenter.getDirectly("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(regCenter.getChildrenKeys("/test/state/datasources")).thenReturn(Collections.<String>emptyList());
        Map<String, DataSourceConfiguration> availableDataSourceConfigs = dataSourceService.getAvailableDataSourceConfigurations("sharding_db");
        assertThat(availableDataSourceConfigs.size(), is(4));
    }
}
