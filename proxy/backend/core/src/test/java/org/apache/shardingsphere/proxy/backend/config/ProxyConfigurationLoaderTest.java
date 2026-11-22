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

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.InvalidShardingStrategyConfigCountException;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRuleConfiguration;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

class ProxyConfigurationLoaderTest {
    
    @Test
    void assertLoadEmptyConfiguration() throws IOException {
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load("/conf/empty/");
        assertNull(actual.getModeConfiguration());
        assertTrue(actual.getProperties().isEmpty());
        assertTrue(actual.getRules().isEmpty());
        assertTrue(actual.getDatabaseConfigurations().isEmpty());
    }
    
    @Test
    void assertLoad() throws IOException {
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load("/conf/config_loader/");
        Iterator<RuleConfiguration> actualGlobalRules = actual.getRules().iterator();
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
                + "  providerType: CALCITE\n"
                + "  allQueryUseSQLFederation: true\n");
        writeConfigurationFile(tempDir, "config-compatible.yaml", "databaseName: compatible_db\n"
                + "dataSources:\n"
                + "  ds_0:\n"
                + "    url: jdbc:mock://127.0.0.1/compatible\n");
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load(tempDir.toString());
        assertThat(actual.getRules().size(), is(6));
        Iterator<RuleConfiguration> rules = actual.getRules().iterator();
        assertThat(rules.next(), isA(AuthorityRuleConfiguration.class));
        assertThat(rules.next(), isA(TransactionRuleConfiguration.class));
        assertThat(rules.next(), isA(SQLParserRuleConfiguration.class));
        assertThat(rules.next(), isA(SQLTranslatorRuleConfiguration.class));
        assertThat(rules.next(), isA(SQLFederationRuleConfiguration.class));
        RuleConfiguration actualGlobalClockRule = rules.next();
        assertThat(actualGlobalClockRule, isA(GlobalClockRuleConfiguration.class));
        assertThat(((GlobalClockRuleConfiguration) actualGlobalClockRule).getProvider(), is("LOCAL"));
        assertNotNull(actual.getDatabaseConfigurations().get("compatible_db").getDataSources());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertLoadWithUnknownRuleTagName(@TempDir final Path tempDir) throws IOException {
        new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.emptyList());
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
    
    @Test
    void assertLoadWithFilesystemPath(@TempDir final Path tempDir) throws IOException {
        writeConfigurationFile(tempDir, "global.yaml", "");
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load(tempDir.toString());
        assertNotNull(actual.getProperties());
        assertTrue(actual.getRules().isEmpty());
        assertTrue(actual.getDatabaseConfigurations().isEmpty());
    }
    
    @Test
    void assertLoadWithDuplicateGlobalAndDatabaseDataSource(@TempDir final Path tempDir) throws IOException {
        writeConfigurationFile(tempDir, "global.yaml", "dataSources:\n  ds_0:\n    url: jdbc:mock://127.0.0.1/global\n");
        writeConfigurationFile(tempDir, "database-duplicate.yaml", "databaseName: foo_db\ndataSources:\n  ds_0:\n    url: jdbc:mock://127.0.0.1/local\n");
        DuplicateStorageUnitException actual = assertThrows(DuplicateStorageUnitException.class, () -> ProxyConfigurationLoader.load(tempDir.toString()));
        assertThat(actual.getMessage(), is("Duplicate storage unit names 'ds_0' on database 'foo_db'."));
    }
    
    @Test
    void assertLoadWithHyphenDatabaseName(@TempDir final Path tempDir) throws IOException {
        writeConfigurationFile(tempDir, "global.yaml", "");
        writeConfigurationFile(tempDir, "database-invalid.yaml", "databaseName: invalid-db\n");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> ProxyConfigurationLoader.load(tempDir.toString()));
        assertThat(actual.getMessage(),
                is("Database name `invalid-db` is invalid, the database name should start with a letter and can contain letters, numbers and underscores only."));
    }
    
    @Test
    void assertLoadWithCompatibleServerYamlOnly(@TempDir final Path tempDir) throws IOException {
        writeConfigurationFile(tempDir, "server.yaml", "authority:\n"
                + "  users:\n"
                + "    - user: root\n"
                + "      password: root\n");
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load(tempDir.toString());
        assertNotNull(actual.getProperties());
        assertFalse(actual.getRules().isEmpty());
        assertTrue(actual.getDatabaseConfigurations().isEmpty());
    }
    
    @Test
    void assertLoadFromClasspathWhenPathDoesNotExistOnFilesystem() throws IOException {
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load("/conf/config_loader/");
        assertNotNull(actual.getProperties());
        assertThat(actual.getDatabaseConfigurations().size(), is(3));
    }
    
    @Test
    void assertLoadFromClasspathWithCompatibleServerYaml() throws IOException {
        ProxyConfigurationLoadResult actual = ProxyConfigurationLoader.load("/conf/compatible_server_yaml/");
        assertNotNull(actual.getProperties());
        assertFalse(actual.getRules().isEmpty());
        assertTrue(actual.getDatabaseConfigurations().isEmpty());
    }
    
    @Test
    void assertLoadWithRuleConversionFailure(@TempDir final Path tempDir) throws IOException {
        writeConfigurationFile(tempDir, "global.yaml", "dataSources:\n  global_ds:\n    url: jdbc:mock://127.0.0.1/global\n");
        writeConfigurationFile(tempDir, "database-invalid-rule.yaml", "databaseName: foo_db\ndataSources:\n  ds_0:\n    url: jdbc:mock://127.0.0.1/local\n"
                + "rules:\n  - !SHARDING\n    defaultDatabaseStrategy:\n      standard:\n        shardingColumn: id\n        shardingAlgorithmName: fixture\n      none: {}\n");
        try (
                MockedStatic<DataSourcePoolCreator> mockedPoolCreator = mockStatic(DataSourcePoolCreator.class);
                MockedConstruction<DataSourceGeneratedDatabaseConfiguration> mockedDatabaseConfigs = mockConstruction(DataSourceGeneratedDatabaseConfiguration.class)) {
            assertThrows(InvalidShardingStrategyConfigCountException.class, () -> ProxyConfigurationLoader.load(tempDir.toString()));
            mockedPoolCreator.verifyNoInteractions();
            assertTrue(mockedDatabaseConfigs.constructed().isEmpty());
        }
    }
    
    private void assertShardingRuleConfiguration(final ProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("sharding_db"));
        assertThat(actual.getDataSources().size(), is(2));
        assertDataSourceConfiguration(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/ds_0");
        assertDataSourceConfiguration(actual.getDataSources().get("ds_1"), "jdbc:mysql://127.0.0.1:3306/ds_1");
        Optional<ShardingRuleConfiguration> shardingRuleConfig = actual.getRules().stream().filter(ShardingRuleConfiguration.class::isInstance).findFirst().map(ShardingRuleConfiguration.class::cast);
        assertTrue(shardingRuleConfig.isPresent());
        assertShardingRuleConfiguration(shardingRuleConfig.get());
        assertFalse(actual.getRules().stream().filter(EncryptRuleConfiguration.class::isInstance).findFirst().map(EncryptRuleConfiguration.class::cast).isPresent());
    }
    
    private void assertShardingRuleConfiguration(final ShardingRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().iterator().next().getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(((StandardShardingStrategyConfiguration) actual.getTables().iterator().next().getDatabaseShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(((StandardShardingStrategyConfiguration) actual.getTables().iterator().next().getDatabaseShardingStrategy()).getShardingAlgorithmName(), is("database_inline"));
        assertThat(((StandardShardingStrategyConfiguration) actual.getTables().iterator().next().getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(((StandardShardingStrategyConfiguration) actual.getTables().iterator().next().getTableShardingStrategy()).getShardingAlgorithmName(), is("table_inline"));
        assertThat(actual.getDefaultDatabaseShardingStrategy(), isA(NoneShardingStrategyConfiguration.class));
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("readwrite_splitting_db"));
        assertThat(actual.getDataSources().size(), is(3));
        assertDataSourceConfiguration(actual.getDataSources().get("write_ds"), "jdbc:mysql://127.0.0.1:3306/write_ds");
        assertDataSourceConfiguration(actual.getDataSources().get("read_ds_0"), "jdbc:mysql://127.0.0.1:3306/read_ds_0");
        assertDataSourceConfiguration(actual.getDataSources().get("read_ds_1"), "jdbc:mysql://127.0.0.1:3306/read_ds_1");
        assertFalse(actual.getRules().stream().filter(ShardingRuleConfiguration.class::isInstance).findFirst().map(ShardingRuleConfiguration.class::cast).isPresent());
        assertFalse(actual.getRules().stream().filter(EncryptRuleConfiguration.class::isInstance).findFirst().map(EncryptRuleConfiguration.class::cast).isPresent());
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = actual.getRules().stream()
                .filter(ReadwriteSplittingRuleConfiguration.class::isInstance).findFirst().map(ReadwriteSplittingRuleConfiguration.class::cast);
        assertTrue(ruleConfig.isPresent());
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : ruleConfig.get().getDataSourceGroups()) {
            assertReadwriteSplittingRuleConfiguration(each);
        }
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingDataSourceGroupRuleConfiguration actual) {
        assertThat(actual.getWriteDataSourceName(), is("write_ds"));
        assertThat(actual.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    private void assertEncryptRuleConfiguration(final ProxyDatabaseConfiguration actual) {
        assertThat(actual.getDatabaseName(), is("encrypt_db"));
        assertThat(actual.getDataSources().size(), is(1));
        assertDataSourceConfiguration(actual.getDataSources().get("ds_0"), "jdbc:mysql://127.0.0.1:3306/encrypt_ds");
        assertFalse(actual.getRules().stream().filter(ShardingRuleConfiguration.class::isInstance).findFirst().map(ShardingRuleConfiguration.class::cast).isPresent());
        Optional<EncryptRuleConfiguration> encryptRuleConfig = actual.getRules().stream().filter(EncryptRuleConfiguration.class::isInstance).findFirst().map(EncryptRuleConfiguration.class::cast);
        assertTrue(encryptRuleConfig.isPresent());
        assertEncryptRuleConfiguration(encryptRuleConfig.get());
    }
    
    private void assertEncryptRuleConfiguration(final EncryptRuleConfiguration actual) {
        assertThat(actual.getEncryptors().size(), is(1));
        assertTrue(actual.getEncryptors().containsKey("aes_encryptor"));
        AlgorithmConfiguration aesEncryptAlgorithmConfig = actual.getEncryptors().get("aes_encryptor");
        assertThat(aesEncryptAlgorithmConfig.getType(), is("AES"));
        assertThat(aesEncryptAlgorithmConfig.getProps().getProperty("aes-key-value"), is("123456abc"));
        assertThat(aesEncryptAlgorithmConfig.getProps().getProperty("digest-algorithm-name"), is("SHA-1"));
    }
    
    private void assertDataSourceConfiguration(final DataSourceConfiguration actual, final String expectedURL) {
        assertThat(actual.getConnection().getDriverClassName(), is("com.mysql.cj.jdbc.Driver"));
        assertThat(actual.getConnection().getUrl(), is(expectedURL));
        assertThat(actual.getConnection().getUsername(), is("root"));
        assertNull(actual.getConnection().getPassword());
        assertThat(actual.getPool().getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(actual.getPool().getIdleTimeoutMilliseconds(), is(60000L));
        assertThat(actual.getPool().getMaxLifetimeMilliseconds(), is(1800000L));
        assertThat(actual.getPool().getMaxPoolSize(), is(50));
    }
    
    private void writeConfigurationFile(final Path directory, final String fileName, final String content) throws IOException {
        Files.write(directory.resolve(fileName), content.getBytes(StandardCharsets.UTF_8));
    }
}
