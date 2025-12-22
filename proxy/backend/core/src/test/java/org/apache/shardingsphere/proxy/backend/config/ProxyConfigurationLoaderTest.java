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

import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.globalclock.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.yaml.config.YamlSQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRuleConfiguration;
import org.apache.shardingsphere.transaction.yaml.config.YamlTransactionRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class ProxyConfigurationLoaderTest {
    
    @Test
    void assertLoadEmptyConfiguration() throws IOException {
        YamlProxyConfiguration actual = ProxyConfigurationLoader.load("/conf/empty/");
        YamlProxyServerConfiguration serverConfig = actual.getServerConfiguration();
        assertNull(serverConfig.getMode());
        assertNull(serverConfig.getAuthority());
        assertNull(serverConfig.getLabels());
        assertTrue(serverConfig.getProps().isEmpty());
        assertTrue(serverConfig.getRules().isEmpty());
        assertTrue(actual.getDatabaseConfigurations().isEmpty());
    }
    
    @Test
    void assertLoad() throws IOException {
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
    
    @Test
    void assertLoadWithCompatibleConfigAndAllGlobalRules(@TempDir final Path tempDir) throws IOException {
        writeConfigurationFile(tempDir, "server.yaml", "rules:\n"
                + "  - !GLOBAL_CLOCK\n"
                + "    type: REMOVED\n"
                + "    provider: REMOVED\n"
                + "authority:\n"
                + "  users:\n"
                + "    - user: root\n"
                + "      password: root\n"
                + "transaction:\n"
                + "  providerType: Atomikos\n"
                + "globalClock:\n"
                + "  type: FIXTURE\n"
                + "  provider: LOCAL\n"
                + "  enabled: true\n"
                + "sqlParser: {}\n"
                + "sqlTranslator:\n"
                + "  type: TRANSLATOR\n"
                + "sqlFederation:\n"
                + "  sqlFederationEnabled: true\n"
                + "  allQueryUseSQLFederation: true\n");
        writeConfigurationFile(tempDir, "config-compatible.yaml", "databaseName: compatible_db\n"
                + "dataSources:\n"
                + "  ds_0:\n"
                + "    url: jdbc:mock://127.0.0.1/compatible\n");
        YamlProxyConfiguration actual = ProxyConfigurationLoader.load(tempDir.toString());
        YamlProxyServerConfiguration serverConfig = actual.getServerConfiguration();
        assertThat(serverConfig.getRules().size(), is(6));
        Iterator<YamlRuleConfiguration> rules = serverConfig.getRules().iterator();
        assertThat(rules.next(), isA(YamlAuthorityRuleConfiguration.class));
        assertThat(rules.next(), isA(YamlTransactionRuleConfiguration.class));
        YamlRuleConfiguration globalClockRule = rules.next();
        assertThat(globalClockRule, isA(YamlGlobalClockRuleConfiguration.class));
        assertThat(((YamlGlobalClockRuleConfiguration) globalClockRule).getProvider(), is("LOCAL"));
        assertThat(rules.next(), isA(YamlSQLParserRuleConfiguration.class));
        assertThat(rules.next(), isA(YamlSQLTranslatorRuleConfiguration.class));
        assertThat(rules.next(), isA(YamlSQLFederationRuleConfiguration.class));
        assertNotNull(actual.getDatabaseConfigurations().get("compatible_db").getDataSources());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertLoadWithUnknownRuleTagName(@TempDir final Path tempDir) throws IOException {
        Collection<YamlRuleConfigurationSwapper> originalSwappers = ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class);
        AtomicInteger yamlRuleConfigurationSwapperCallTimes = new AtomicInteger();
        try (MockedStatic<ShardingSphereServiceLoader> mockedStatic = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class))
                    .thenAnswer(invocation -> yamlRuleConfigurationSwapperCallTimes.getAndIncrement() < 2 ? originalSwappers : Collections.emptyList());
            writeConfigurationFile(tempDir, "global.yaml", "");
            writeConfigurationFile(tempDir, "database-unknown-rule-tag.yaml", "databaseName: unknown_rule_tag_db\n"
                    + "dataSources:\n"
                    + "  ds_0:\n"
                    + "    url: jdbc:mock://127.0.0.1/unknown\n"
                    + "rules:\n"
                    + "  - !FIXTURE\n"
                    + "    unique: duplicated\n"
                    + "  - !FIXTURE\n"
                    + "    unique: duplicated\n");
            IllegalStateException actual = assertThrows(IllegalStateException.class, () -> ProxyConfigurationLoader.load(tempDir.toString()));
            assertThat(actual.getMessage(), is("Not find rule tag name of class class " + MockedRuleConfiguration.class.getName()));
        }
    }
    
    private void assertShardingRuleConfiguration(final YamlProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("sharding_db"));
        assertThat(actual.getDataSources().size(), is(2));
        assertDataSourceConfiguration(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/ds_0");
        assertDataSourceConfiguration(actual.getDataSources().get("ds_1"), "jdbc:mysql://127.0.0.1:3306/ds_1");
        Optional<YamlShardingRuleConfiguration> shardingRuleConfig = actual.getRules().stream()
                .filter(YamlShardingRuleConfiguration.class::isInstance).findFirst().map(YamlShardingRuleConfiguration.class::cast);
        assertTrue(shardingRuleConfig.isPresent());
        assertShardingRuleConfiguration(shardingRuleConfig.get());
        assertFalse(
                actual.getRules().stream().filter(YamlEncryptRuleConfiguration.class::isInstance).findFirst().map(YamlEncryptRuleConfiguration.class::cast).isPresent());
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
        assertFalse(actual.getRules().stream().filter(YamlShardingRuleConfiguration.class::isInstance).findFirst().map(YamlShardingRuleConfiguration.class::cast).isPresent());
        assertFalse(
                actual.getRules().stream().filter(YamlEncryptRuleConfiguration.class::isInstance).findFirst().map(YamlEncryptRuleConfiguration.class::cast).isPresent());
        Optional<YamlReadwriteSplittingRuleConfiguration> ruleConfig = actual.getRules().stream()
                .filter(YamlReadwriteSplittingRuleConfiguration.class::isInstance).findFirst().map(YamlReadwriteSplittingRuleConfiguration.class::cast);
        assertTrue(ruleConfig.isPresent());
        for (YamlReadwriteSplittingDataSourceGroupRuleConfiguration each : ruleConfig.get().getDataSourceGroups().values()) {
            assertReadwriteSplittingRuleConfiguration(each);
        }
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final YamlReadwriteSplittingDataSourceGroupRuleConfiguration actual) {
        assertThat(actual.getWriteDataSourceName(), is("write_ds"));
        assertThat(actual.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    private void assertEncryptRuleConfiguration(final YamlProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("encrypt_db"));
        assertThat(actual.getDataSources().size(), is(1));
        assertDataSourceConfiguration(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/encrypt_ds");
        assertFalse(actual.getRules().stream()
                .filter(YamlShardingRuleConfiguration.class::isInstance).findFirst().map(YamlShardingRuleConfiguration.class::cast).isPresent());
        Optional<YamlEncryptRuleConfiguration> encryptRuleConfig = actual.getRules().stream()
                .filter(YamlEncryptRuleConfiguration.class::isInstance).findFirst().map(YamlEncryptRuleConfiguration.class::cast);
        assertTrue(encryptRuleConfig.isPresent());
        assertEncryptRuleConfiguration(encryptRuleConfig.get());
    }
    
    private void assertEncryptRuleConfiguration(final YamlEncryptRuleConfiguration actual) {
        assertThat(actual.getEncryptors().size(), is(1));
        assertTrue(actual.getEncryptors().containsKey("aes_encryptor"));
        YamlAlgorithmConfiguration aesEncryptAlgorithmConfig = actual.getEncryptors().get("aes_encryptor");
        assertThat(aesEncryptAlgorithmConfig.getType(), is("AES"));
        assertThat(aesEncryptAlgorithmConfig.getProps().getProperty("aes-key-value"), is("123456abc"));
        assertThat(aesEncryptAlgorithmConfig.getProps().getProperty("digest-algorithm-name"), is("SHA-1"));
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
    
    private void writeConfigurationFile(final Path directory, final String fileName, final String content) throws IOException {
        Files.write(directory.resolve(fileName), content.getBytes(StandardCharsets.UTF_8));
    }
}
