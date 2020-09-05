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

package org.apache.shardingsphere.driver.governance.internal.datasource;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.governance.core.common.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.governance.context.schema.GovernanceSchemaContexts;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GovernanceShardingSphereDataSourceTest {
    
    private static GovernanceSchemaContexts governanceSchemaContexts;
    
    @BeforeClass
    public static void setUp() throws SQLException, IOException, URISyntaxException {
        SchemaContexts schemaContexts = getShardingSphereDataSource().getSchemaContexts();
        GovernanceShardingSphereDataSource governanceDataSource = new GovernanceShardingSphereDataSource(schemaContexts.getDefaultSchemaContext().getSchema().getDataSources(),
                schemaContexts.getDefaultSchemaContext().getSchema().getConfigurations(), schemaContexts.getProps().getProps(), getGovernanceConfiguration());
        governanceSchemaContexts = (GovernanceSchemaContexts) governanceDataSource.getSchemaContexts();
    }
    
    private static ShardingSphereDataSource getShardingSphereDataSource() throws IOException, SQLException, URISyntaxException {
        File yamlFile = new File(GovernanceShardingSphereDataSourceTest.class.getResource("/yaml/unit/sharding.yaml").toURI());
        return (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
    }
    
    private static GovernanceConfiguration getGovernanceConfiguration() {
        return new GovernanceConfiguration("test_name", getRegistryCenterConfiguration(), getConfigCenterConfiguration(), true);
    }
    
    private static GovernanceCenterConfiguration getRegistryCenterConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("overwrite", "true");
        return new GovernanceCenterConfiguration("REG_TEST", "localhost:3181", properties);
    }
    
    private static GovernanceCenterConfiguration getConfigCenterConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("overwrite", "true");
        return new GovernanceCenterConfiguration("CONFIG_TEST", "localhost:3181", properties);
    }
    
    @Test
    public void assertInitializeGovernanceShardingSphereDataSource() throws SQLException {
        assertThat(new GovernanceShardingSphereDataSource(getGovernanceConfiguration()).getConnection(), instanceOf(Connection.class));
    }
    
    @Test
    public void assertRenewRules() throws SQLException {
        governanceSchemaContexts.renew(new RuleConfigurationsChangedEvent(DefaultSchema.LOGIC_NAME, Arrays.asList(getShardingRuleConfiguration(), getMasterSlaveRuleConfiguration())));
        assertThat(((ShardingRule) governanceSchemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next()).getTableRules().size(), is(1));
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("logic_table", "ds_ms.table_${0..1}"));
        return result;
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        MasterSlaveDataSourceRuleConfiguration dataSourceConfiguration = new MasterSlaveDataSourceRuleConfiguration("ds_ms", "ds_m", Collections.singletonList("ds_s"), "roundRobin");
        return new MasterSlaveRuleConfiguration(
                Collections.singleton(dataSourceConfiguration), ImmutableMap.of("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties())));
    }
    
    @Test
    public void assertRenewDataSource() throws SQLException {
        governanceSchemaContexts.renew(new DataSourceChangedEvent(DefaultSchema.LOGIC_NAME, getDataSourceConfigurations()));
        assertThat(governanceSchemaContexts.getDefaultSchemaContext().getSchema().getDataSources().size(), is(3));
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(3, 1);
        result.put("ds_m", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_s", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_0", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertRenewProperties() {
        governanceSchemaContexts.renew(getPropertiesChangedEvent());
        assertThat(governanceSchemaContexts.getProps().getProps().getProperty("sql.show"), is("true"));
    }
    
    private PropertiesChangedEvent getPropertiesChangedEvent() {
        Properties props = new Properties();
        props.setProperty("sql.show", "true");
        return new PropertiesChangedEvent(props);
    }
    
    @Test
    public void assertRenewDisabledState() {
        governanceSchemaContexts.renew(new DisabledStateChangedEvent(new GovernanceSchema("logic_db.ds_s"), true));
    }
}
