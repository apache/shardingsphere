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

package org.apache.shardingsphere.proxy.backend.config;

import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ProxyConfigurationLoaderTest {
    
    @Test
    public void assertLoad() throws IOException {
        YamlProxyConfiguration actual = ProxyConfigurationLoader.load("/conf/config_loader/");
        Iterator<YamlRuleConfiguration> actualGlobalRules = actual.getServerConfiguration().getRules().iterator();
        // TODO assert mode
        // TODO assert authority rule
        actualGlobalRules.next();
        assertThat(actual.getDatabaseConfigurations().size(), is(3));
        assertShardingRuleConfiguration(actual.getDatabaseConfigurations().get("sharding_db"));
        assertReadwriteSplittingRuleConfiguration(actual.getDatabaseConfigurations().get("readwrite_splitting_db"));
        assertEncryptRuleConfiguration(actual.getDatabaseConfigurations().get("encrypt_db"));
    }
    
    private void assertShardingRuleConfiguration(final YamlProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("sharding_db"));
        assertThat(actual.getDataSources().size(), is(2));
        assertDataSourceConfiguration(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/ds_0");
        assertDataSourceConfiguration(actual.getDataSources().get("ds_1"), "jdbc:mysql://127.0.0.1:3306/ds_1");
        Optional<YamlShardingRuleConfiguration> shardingRuleConfig = actual.getRules().stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(each -> (YamlShardingRuleConfiguration) each);
        assertTrue(shardingRuleConfig.isPresent());
        assertShardingRuleConfiguration(shardingRuleConfig.get());
        assertFalse(actual.getRules().stream().filter(each -> each instanceof YamlEncryptRuleConfiguration).findFirst().map(each -> (YamlEncryptRuleConfiguration) each).isPresent());
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
    
    private void assertReadwriteSplittingRuleConfiguration(final YamlProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("readwrite_splitting_db"));
        assertThat(actual.getDataSources().size(), is(3));
        assertDataSourceConfiguration(actual.getDataSources().get("write_ds"), "jdbc:mysql://127.0.0.1:3306/write_ds");
        assertDataSourceConfiguration(actual.getDataSources().get("read_ds_0"), "jdbc:mysql://127.0.0.1:3306/read_ds_0");
        assertDataSourceConfiguration(actual.getDataSources().get("read_ds_1"), "jdbc:mysql://127.0.0.1:3306/read_ds_1");
        assertFalse(actual.getRules().stream().filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(each -> (YamlShardingRuleConfiguration) each).isPresent());
        assertFalse(actual.getRules().stream().filter(each -> each instanceof YamlEncryptRuleConfiguration).findFirst().map(each -> (YamlEncryptRuleConfiguration) each).isPresent());
        Optional<YamlReadwriteSplittingRuleConfiguration> ruleConfig = actual.getRules().stream()
                .filter(each -> each instanceof YamlReadwriteSplittingRuleConfiguration).findFirst().map(each -> (YamlReadwriteSplittingRuleConfiguration) each);
        assertTrue(ruleConfig.isPresent());
        for (YamlReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.get().getDataSources().values()) {
            assertReadwriteSplittingRuleConfiguration(each);
        }
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final YamlReadwriteSplittingDataSourceRuleConfiguration actual) {
        assertNotNull(actual.getStaticStrategy());
        assertThat(actual.getStaticStrategy().getWriteDataSourceName(), is("write_ds"));
        assertThat(actual.getStaticStrategy().getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    private void assertEncryptRuleConfiguration(final YamlProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("encrypt_db"));
        assertThat(actual.getDataSources().size(), is(1));
        assertDataSourceConfiguration(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/encrypt_ds");
        assertFalse(actual.getRules().stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(each -> (YamlShardingRuleConfiguration) each).isPresent());
        Optional<YamlEncryptRuleConfiguration> encryptRuleConfig = actual.getRules().stream()
                .filter(each -> each instanceof YamlEncryptRuleConfiguration).findFirst().map(each -> (YamlEncryptRuleConfiguration) each);
        assertTrue(encryptRuleConfig.isPresent());
        assertEncryptRuleConfiguration(encryptRuleConfig.get());
    }
    
    private void assertEncryptRuleConfiguration(final YamlEncryptRuleConfiguration actual) {
        assertThat(actual.getEncryptors().size(), is(2));
        assertTrue(actual.getEncryptors().containsKey("aes_encryptor"));
        assertTrue(actual.getEncryptors().containsKey("md5_encryptor"));
        YamlAlgorithmConfiguration aesEncryptAlgorithmConfig = actual.getEncryptors().get("aes_encryptor");
        assertThat(aesEncryptAlgorithmConfig.getType(), is("AES"));
        assertThat(aesEncryptAlgorithmConfig.getProps().getProperty("aes-key-value"), is("123456abc"));
        YamlAlgorithmConfiguration md5EncryptAlgorithmConfig = actual.getEncryptors().get("md5_encryptor");
        assertThat(md5EncryptAlgorithmConfig.getType(), is("MD5"));
    }
    
    private void assertDataSourceConfiguration(final YamlProxyDataSourceConfiguration actual, final String expectedURL) {
        assertThat(actual.getUrl(), is(expectedURL));
        assertThat(actual.getUsername(), is("root"));
        assertNull(actual.getPassword());
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(60000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(1800000L));
        assertThat(actual.getMaxPoolSize(), is(50));
    }
}
