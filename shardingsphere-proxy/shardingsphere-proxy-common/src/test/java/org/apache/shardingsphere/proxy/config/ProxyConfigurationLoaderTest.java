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

package org.apache.shardingsphere.proxy.config;

import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.rule.YamlPrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ProxyConfigurationLoaderTest {
    
    @Test
    public void assertLoad() throws IOException {
        YamlProxyConfiguration actual = ProxyConfigurationLoader.load("/conf/");
        assertThat(actual.getServerConfiguration().getGovernance().getRegistryCenter().getServerLists(), is("localhost:2181"));
        assertThat(actual.getRuleConfigurations().size(), is(3));
        assertShardingRuleConfiguration(actual.getRuleConfigurations().get("sharding_db"));
        assertPrimaryReplicaReplicationRuleConfiguration(actual.getRuleConfigurations().get("primary_replica_db"));
        assertEncryptRuleConfiguration(actual.getRuleConfigurations().get("encrypt_db"));
    }
    
    private void assertShardingRuleConfiguration(final YamlProxyRuleConfiguration actual) {
        assertThat(actual.getSchemaName(), is("sharding_db"));
        assertThat(actual.getDataSources().size(), is(2));
        assertNull(actual.getDataSource());
        assertDataSourceParameter(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/ds_0");
        assertDataSourceParameter(actual.getDataSources().get("ds_1"), "jdbc:mysql://127.0.0.1:3306/ds_1");
        Optional<YamlShardingRuleConfiguration> shardingRuleConfiguration = actual.getRules().stream().filter(
            each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(configuration -> (YamlShardingRuleConfiguration) configuration);
        assertTrue(shardingRuleConfiguration.isPresent());
        assertShardingRuleConfiguration(shardingRuleConfiguration.get());
        assertFalse(actual.getRules().stream().filter(
            each -> each instanceof YamlEncryptRuleConfiguration).findFirst().map(configuration -> (YamlEncryptRuleConfiguration) configuration).isPresent());
    }
    
    private void assertShardingRuleConfiguration(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().get("t_order").getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.getTables().get("t_order").getDatabaseStrategy().getStandard().getShardingColumn(), is("user_id"));
        assertThat(actual.getTables().get("t_order").getDatabaseStrategy().getStandard().getShardingAlgorithmName(), is("database_inline"));
        assertThat(actual.getTables().get("t_order").getTableStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getTables().get("t_order").getTableStrategy().getStandard().getShardingAlgorithmName(), is("table_inline"));
        assertNotNull(actual.getDefaultDatabaseStrategy().getNone());
    }
    
    private void assertPrimaryReplicaReplicationRuleConfiguration(final YamlProxyRuleConfiguration actual) {
        assertThat(actual.getSchemaName(), is("primary_replica_db"));
        assertThat(actual.getDataSources().size(), is(3));
        assertNull(actual.getDataSource());
        assertDataSourceParameter(actual.getDataSources().get("primary_ds"), "jdbc:mysql://127.0.0.1:3306/primary_ds");
        assertDataSourceParameter(actual.getDataSources().get("replica_ds_0"), "jdbc:mysql://127.0.0.1:3306/replica_ds_0");
        assertDataSourceParameter(actual.getDataSources().get("replica_ds_1"), "jdbc:mysql://127.0.0.1:3306/replica_ds_1");
        assertFalse(actual.getRules().stream().filter(
            each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(configuration -> (YamlShardingRuleConfiguration) configuration).isPresent());
        assertFalse(actual.getRules().stream().filter(
            each -> each instanceof YamlEncryptRuleConfiguration).findFirst().map(configuration -> (YamlEncryptRuleConfiguration) configuration).isPresent());
        Optional<YamlPrimaryReplicaReplicationRuleConfiguration> ruleConfig = actual.getRules().stream().filter(
            each -> each instanceof YamlPrimaryReplicaReplicationRuleConfiguration).findFirst().map(configuration -> (YamlPrimaryReplicaReplicationRuleConfiguration) configuration);
        assertTrue(ruleConfig.isPresent());
        for (YamlPrimaryReplicaReplicationDataSourceRuleConfiguration each : ruleConfig.get().getDataSources().values()) {
            assertPrimaryReplicaReplicationRuleConfiguration(each);
        }
    }
    
    private void assertPrimaryReplicaReplicationRuleConfiguration(final YamlPrimaryReplicaReplicationDataSourceRuleConfiguration actual) {
        assertThat(actual.getName(), is("pr_ds"));
        assertThat(actual.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(actual.getReplicaDataSourceNames().size(), is(2));
        Iterator<String> slaveDataSourceNames = actual.getReplicaDataSourceNames().iterator();
        assertThat(slaveDataSourceNames.next(), is("replica_ds_0"));
        assertThat(slaveDataSourceNames.next(), is("replica_ds_1"));
    }
    
    private void assertEncryptRuleConfiguration(final YamlProxyRuleConfiguration actual) {
        assertThat(actual.getSchemaName(), is("encrypt_db"));
        assertThat(actual.getDataSources().size(), is(1));
        assertNotNull(actual.getDataSource());
        assertDataSourceParameter(actual.getDataSources().get("dataSource"), "jdbc:mysql://127.0.0.1:3306/encrypt_ds");
        assertFalse(actual.getRules().stream().filter(
            each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(configuration -> (YamlShardingRuleConfiguration) configuration).isPresent());
        Optional<YamlEncryptRuleConfiguration> encryptRuleConfiguration = actual.getRules().stream().filter(
            each -> each instanceof YamlEncryptRuleConfiguration).findFirst().map(configuration -> (YamlEncryptRuleConfiguration) configuration);
        assertTrue(encryptRuleConfiguration.isPresent());
        assertEncryptRuleConfiguration(encryptRuleConfiguration.get());
    }
    
    private void assertEncryptRuleConfiguration(final YamlEncryptRuleConfiguration actual) {
        assertThat(actual.getEncryptors().size(), is(2));
        assertTrue(actual.getEncryptors().containsKey("aes_encryptor"));
        assertTrue(actual.getEncryptors().containsKey("md5_encryptor"));
        YamlShardingSphereAlgorithmConfiguration aesEncryptAlgorithmConfig = actual.getEncryptors().get("aes_encryptor");
        assertThat(aesEncryptAlgorithmConfig.getType(), is("AES"));
        assertThat(aesEncryptAlgorithmConfig.getProps().getProperty("aes-key-value"), is("123456abc"));
        YamlShardingSphereAlgorithmConfiguration md5EncryptAlgorithmConfig = actual.getEncryptors().get("md5_encryptor");
        assertThat(md5EncryptAlgorithmConfig.getType(), is("MD5"));
    }
    
    private void assertDataSourceParameter(final YamlDataSourceParameter actual, final String expectedURL) {
        assertThat(actual.getUrl(), is(expectedURL));
        assertThat(actual.getUsername(), is("root"));
        assertNull(actual.getPassword());
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(60000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(1800000L));
        assertThat(actual.getMaxPoolSize(), is(50));
    }
}
