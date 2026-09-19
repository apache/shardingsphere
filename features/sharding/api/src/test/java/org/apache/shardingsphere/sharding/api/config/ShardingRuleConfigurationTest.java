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

package org.apache.shardingsphere.sharding.api.config;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingRuleConfigurationTest {
    
    @Test
    void assertGetLogicTableNames() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().addAll(Arrays.asList(new ShardingTableRuleConfiguration("foo_tbl", "foo_tbl_0"), new ShardingTableRuleConfiguration("bar_tbl", "bar_tbl_0")));
        Collection<String> actual = ruleConfig.getLogicTableNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("foo_tbl"));
        assertTrue(actual.contains("bar_tbl"));
        assertTrue(actual.contains("FOO_TBL"));
        assertTrue(actual.contains("BAR_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final ShardingRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        ShardingRuleConfiguration tableRuleConfig = new ShardingRuleConfiguration();
        tableRuleConfig.getTables().add(new ShardingTableRuleConfiguration("foo_tbl", null));
        ShardingRuleConfiguration defaultShardingColumnRuleConfig = new ShardingRuleConfiguration();
        defaultShardingColumnRuleConfig.setDefaultShardingColumn("user_id");
        defaultShardingColumnRuleConfig.getTables().add(createTableRuleConfiguration(new StandardShardingStrategyConfiguration(null, "foo_standard")));
        defaultShardingColumnRuleConfig.getShardingAlgorithms().put("foo_standard", new AlgorithmConfiguration("FIXTURE", new Properties()));
        return Stream.of(
                Arguments.of("Empty configuration", new ShardingRuleConfiguration()),
                Arguments.of("Table with nullable optional values", tableRuleConfig),
                Arguments.of("Standard strategy with default sharding column", defaultShardingColumnRuleConfig),
                Arguments.of("Complete configuration", createValidRuleConfiguration()));
    }
    
    private static ShardingRuleConfiguration createValidRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableConfig = new ShardingTableRuleConfiguration("foo_tbl", "foo_ds.foo_tbl_${0..1}");
        tableConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "foo_standard"));
        tableConfig.setTableShardingStrategy(new ComplexShardingStrategyConfiguration("order_id,user_id", "foo_complex"));
        tableConfig.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("foo_auditor"), false));
        result.getTables().add(tableConfig);
        ShardingAutoTableRuleConfiguration autoTableConfig = new ShardingAutoTableRuleConfiguration("foo_auto_tbl", "foo_ds_${0..1}");
        autoTableConfig.setShardingStrategy(new HintShardingStrategyConfiguration("foo_hint"));
        autoTableConfig.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("foo_auditor"), true));
        result.getAutoTables().add(autoTableConfig);
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo_group", "foo_tbl,foo_item"));
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "foo_standard"));
        result.setDefaultTableShardingStrategy(new ComplexShardingStrategyConfiguration("order_id,user_id", "foo_complex"));
        result.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "foo_key_generator"));
        result.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("foo_auditor"), false));
        result.setDefaultShardingColumn("user_id");
        result.getKeyGenerateStrategies().put("foo_column", new ColumnKeyGenerateStrategiesRuleConfiguration("foo_key_generator", "foo_tbl", "order_id"));
        result.getKeyGenerateStrategies().put("foo_sequence", new SequenceKeyGenerateStrategiesRuleConfiguration("foo_key_generator", "foo_sequence"));
        AlgorithmConfiguration shardingAlgorithmConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        result.getShardingAlgorithms().put("foo_standard", shardingAlgorithmConfig);
        result.getShardingAlgorithms().put("foo_complex", shardingAlgorithmConfig);
        result.getShardingAlgorithms().put("foo_hint", shardingAlgorithmConfig);
        result.getKeyGenerators().put("foo_key_generator", new AlgorithmConfiguration("FIXTURE", new Properties()));
        result.getAuditors().put("foo_auditor", new AlgorithmConfiguration("FIXTURE_AUDIT", new Properties()));
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final ShardingRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        ShardingRuleConfiguration nullTables = new ShardingRuleConfiguration();
        nullTables.setTables(null);
        ShardingRuleConfiguration nullAutoTables = new ShardingRuleConfiguration();
        nullAutoTables.setAutoTables(null);
        ShardingRuleConfiguration nullBindingTableGroups = new ShardingRuleConfiguration();
        nullBindingTableGroups.setBindingTableGroups(null);
        ShardingRuleConfiguration nullKeyGenerateStrategies = new ShardingRuleConfiguration();
        nullKeyGenerateStrategies.setKeyGenerateStrategies(null);
        ShardingRuleConfiguration nullShardingAlgorithms = new ShardingRuleConfiguration();
        nullShardingAlgorithms.setShardingAlgorithms(null);
        ShardingRuleConfiguration nullKeyGenerators = new ShardingRuleConfiguration();
        nullKeyGenerators.setKeyGenerators(null);
        ShardingRuleConfiguration nullAuditors = new ShardingRuleConfiguration();
        nullAuditors.setAuditors(null);
        ShardingTableRuleConfiguration invalidTableStrategy = new ShardingTableRuleConfiguration("foo_tbl", null);
        invalidTableStrategy.setTableShardingStrategy(new ComplexShardingStrategyConfiguration("order_id,user_id", ""));
        ShardingTableRuleConfiguration invalidTableAuditStrategy = new ShardingTableRuleConfiguration("foo_tbl", null);
        invalidTableAuditStrategy.setAuditStrategy(new ShardingAuditStrategyConfiguration(null, false));
        ShardingAutoTableRuleConfiguration nullAutoTableStrategy = new ShardingAutoTableRuleConfiguration("foo_auto_tbl", null);
        ShardingAutoTableRuleConfiguration invalidAutoTableStrategy = new ShardingAutoTableRuleConfiguration("foo_auto_tbl", null);
        invalidAutoTableStrategy.setShardingStrategy(new HintShardingStrategyConfiguration(""));
        ShardingAutoTableRuleConfiguration invalidAutoTableAuditStrategy = createValidAutoTableRuleConfiguration();
        invalidAutoTableAuditStrategy.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton(""), false));
        ShardingRuleConfiguration invalidDefaultDatabaseStrategy = new ShardingRuleConfiguration();
        invalidDefaultDatabaseStrategy.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", ""));
        ShardingRuleConfiguration invalidDefaultTableStrategy = new ShardingRuleConfiguration();
        invalidDefaultTableStrategy.setDefaultTableShardingStrategy(new ComplexShardingStrategyConfiguration("", "foo_complex"));
        ShardingRuleConfiguration invalidDefaultKeyGenerateStrategy = new ShardingRuleConfiguration();
        invalidDefaultKeyGenerateStrategy.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("", "foo_key_generator"));
        ShardingRuleConfiguration invalidDefaultAuditStrategy = new ShardingRuleConfiguration();
        invalidDefaultAuditStrategy.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton(""), false));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        return Stream.of(
                Arguments.of("Null tables", nullTables),
                Arguments.of("Null table", createRuleConfiguration((ShardingTableRuleConfiguration) null)),
                Arguments.of("Null auto tables", nullAutoTables),
                Arguments.of("Null auto table", createRuleConfiguration((ShardingAutoTableRuleConfiguration) null)),
                Arguments.of("Null binding table groups", nullBindingTableGroups),
                Arguments.of("Null binding table group", createRuleConfiguration((ShardingTableReferenceRuleConfiguration) null)),
                Arguments.of("Null key generate strategies", nullKeyGenerateStrategies),
                Arguments.of("Blank key generate strategy name",
                        createKeyGenerateStrategiesRuleConfiguration("", new SequenceKeyGenerateStrategiesRuleConfiguration("foo_key_generator", "foo_sequence"))),
                Arguments.of("Null key generate strategy", createKeyGenerateStrategiesRuleConfiguration("foo_strategy", null)),
                Arguments.of("Null sharding algorithms", nullShardingAlgorithms),
                Arguments.of("Blank sharding algorithm name", createShardingAlgorithmsRuleConfiguration("", algorithmConfig)),
                Arguments.of("Null sharding algorithm", createShardingAlgorithmsRuleConfiguration("foo_sharding", null)),
                Arguments.of("Missing sharding algorithm type", createShardingAlgorithmsRuleConfiguration(
                        "foo_sharding", new AlgorithmConfiguration("MISSING", new Properties()))),
                Arguments.of("Null key generators", nullKeyGenerators),
                Arguments.of("Null key generator", createKeyGeneratorsRuleConfiguration("foo_key_generator", null)),
                Arguments.of("Missing key generator type", createKeyGeneratorsRuleConfiguration(
                        "foo_key_generator", new AlgorithmConfiguration("MISSING", new Properties()))),
                Arguments.of("Null auditors", nullAuditors),
                Arguments.of("Blank auditor name", createAuditorsRuleConfiguration("", algorithmConfig)),
                Arguments.of("Null auditor", createAuditorsRuleConfiguration("foo_auditor", null)),
                Arguments.of("Missing auditor type", createAuditorsRuleConfiguration(
                        "foo_auditor", new AlgorithmConfiguration("MISSING", new Properties()))),
                Arguments.of("Blank table logic name", createRuleConfiguration(new ShardingTableRuleConfiguration("", null))),
                Arguments.of("Invalid table sharding strategy", createRuleConfiguration(invalidTableStrategy)),
                Arguments.of("Invalid table audit strategy", createRuleConfiguration(invalidTableAuditStrategy)),
                Arguments.of("Blank auto table logic name", createRuleConfiguration(createAutoTableRuleConfiguration("", new HintShardingStrategyConfiguration("foo_hint")))),
                Arguments.of("Null auto table strategy", createRuleConfiguration(nullAutoTableStrategy)),
                Arguments.of("Invalid auto table strategy", createRuleConfiguration(invalidAutoTableStrategy)),
                Arguments.of("Invalid auto table audit strategy", createRuleConfiguration(invalidAutoTableAuditStrategy)),
                Arguments.of("Blank binding table group name", createRuleConfiguration(new ShardingTableReferenceRuleConfiguration("", "foo_tbl,foo_item"))),
                Arguments.of("Blank binding table group reference", createRuleConfiguration(new ShardingTableReferenceRuleConfiguration("foo_group", ""))),
                Arguments.of("Invalid default database strategy", invalidDefaultDatabaseStrategy),
                Arguments.of("Invalid default table strategy", invalidDefaultTableStrategy),
                Arguments.of("Invalid default key generate strategy", invalidDefaultKeyGenerateStrategy),
                Arguments.of("Invalid default audit strategy", invalidDefaultAuditStrategy),
                Arguments.of("Blank standard sharding algorithm", createRuleConfiguration(createTableRuleConfiguration(
                        new StandardShardingStrategyConfiguration("user_id", "")))),
                Arguments.of("Blank complex sharding columns", createRuleConfiguration(createTableRuleConfiguration(
                        new ComplexShardingStrategyConfiguration("", "foo_complex")))),
                Arguments.of("Blank complex sharding algorithm", createRuleConfiguration(createTableRuleConfiguration(
                        new ComplexShardingStrategyConfiguration("order_id,user_id", "")))),
                Arguments.of("Blank hint sharding algorithm", createRuleConfiguration(createAutoTableRuleConfiguration(
                        "foo_auto_tbl", new HintShardingStrategyConfiguration("")))),
                Arguments.of("Null audit names", createRuleConfiguration(createTableRuleConfiguration(
                        new ShardingAuditStrategyConfiguration(null, false)))),
                Arguments.of("Blank audit name", createRuleConfiguration(createTableRuleConfiguration(
                        new ShardingAuditStrategyConfiguration(Collections.singleton(""), false)))),
                Arguments.of("Blank default key generate column", createDefaultKeyGenerateStrategyRuleConfiguration(
                        new KeyGenerateStrategyConfiguration("", "foo_key_generator"))),
                Arguments.of("Blank default key generator name", createDefaultKeyGenerateStrategyRuleConfiguration(
                        new KeyGenerateStrategyConfiguration("order_id", ""))),
                Arguments.of("Blank column strategy key generator", createKeyGenerateStrategiesRuleConfiguration("foo_strategy",
                        new ColumnKeyGenerateStrategiesRuleConfiguration("", "foo_tbl", "order_id"))),
                Arguments.of("Blank column strategy logic table", createKeyGenerateStrategiesRuleConfiguration("foo_strategy",
                        new ColumnKeyGenerateStrategiesRuleConfiguration("foo_key_generator", "", "order_id"))),
                Arguments.of("Blank column strategy key column", createKeyGenerateStrategiesRuleConfiguration("foo_strategy",
                        new ColumnKeyGenerateStrategiesRuleConfiguration("foo_key_generator", "foo_tbl", ""))),
                Arguments.of("Blank sequence strategy key generator", createKeyGenerateStrategiesRuleConfiguration("foo_strategy",
                        new SequenceKeyGenerateStrategiesRuleConfiguration("", "foo_sequence"))),
                Arguments.of("Blank sequence strategy sequence", createKeyGenerateStrategiesRuleConfiguration("foo_strategy",
                        new SequenceKeyGenerateStrategiesRuleConfiguration("foo_key_generator", ""))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReferenceRuleConfigurationArguments")
    void assertValidateInvalidReferenceRuleConfiguration(final String name, final ShardingRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidReferenceRuleConfigurationArguments() {
        ShardingRuleConfiguration unconfiguredTableDatabaseShardingAlgorithm = createValidRuleConfiguration();
        unconfiguredTableDatabaseShardingAlgorithm.getTables().iterator().next()
                .setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "bar_standard"));
        ShardingRuleConfiguration unconfiguredTableShardingAlgorithm = createValidRuleConfiguration();
        unconfiguredTableShardingAlgorithm.getTables().iterator().next()
                .setTableShardingStrategy(new ComplexShardingStrategyConfiguration("order_id,user_id", "bar_complex"));
        ShardingRuleConfiguration unconfiguredAutoTableShardingAlgorithm = createValidRuleConfiguration();
        unconfiguredAutoTableShardingAlgorithm.getAutoTables().iterator().next().setShardingStrategy(new HintShardingStrategyConfiguration("bar_hint"));
        ShardingRuleConfiguration unconfiguredDefaultDatabaseShardingAlgorithm = createValidRuleConfiguration();
        unconfiguredDefaultDatabaseShardingAlgorithm.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "bar_standard"));
        ShardingRuleConfiguration unconfiguredDefaultTableShardingAlgorithm = createValidRuleConfiguration();
        unconfiguredDefaultTableShardingAlgorithm.setDefaultTableShardingStrategy(new ComplexShardingStrategyConfiguration("order_id,user_id", "bar_complex"));
        ShardingRuleConfiguration unconfiguredTableAuditor = createValidRuleConfiguration();
        unconfiguredTableAuditor.getTables().iterator().next().setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("bar_auditor"), false));
        ShardingRuleConfiguration unconfiguredAutoTableAuditor = createValidRuleConfiguration();
        unconfiguredAutoTableAuditor.getAutoTables().iterator().next().setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("bar_auditor"), false));
        ShardingRuleConfiguration unconfiguredDefaultAuditor = createValidRuleConfiguration();
        unconfiguredDefaultAuditor.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("bar_auditor"), false));
        ShardingRuleConfiguration unconfiguredDefaultKeyGenerator = createValidRuleConfiguration();
        unconfiguredDefaultKeyGenerator.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "bar_key_generator"));
        ShardingRuleConfiguration unconfiguredKeyGenerateStrategiesKeyGenerator = createValidRuleConfiguration();
        unconfiguredKeyGenerateStrategiesKeyGenerator.getKeyGenerateStrategies().put(
                "bar_column", new ColumnKeyGenerateStrategiesRuleConfiguration("bar_key_generator", "foo_tbl", "order_id"));
        ShardingRuleConfiguration unsupportedKeyGenerateStrategies = createValidRuleConfiguration();
        unsupportedKeyGenerateStrategies.getKeyGenerateStrategies().put("bar_strategy", new UnsupportedKeyGenerateStrategiesConfiguration());
        return Stream.of(
                Arguments.of("Unconfigured table database sharding algorithm", unconfiguredTableDatabaseShardingAlgorithm),
                Arguments.of("Unconfigured table sharding algorithm", unconfiguredTableShardingAlgorithm),
                Arguments.of("Unconfigured auto table sharding algorithm", unconfiguredAutoTableShardingAlgorithm),
                Arguments.of("Unconfigured default database sharding algorithm", unconfiguredDefaultDatabaseShardingAlgorithm),
                Arguments.of("Unconfigured default table sharding algorithm", unconfiguredDefaultTableShardingAlgorithm),
                Arguments.of("Unconfigured table auditor", unconfiguredTableAuditor),
                Arguments.of("Unconfigured auto table auditor", unconfiguredAutoTableAuditor),
                Arguments.of("Unconfigured default auditor", unconfiguredDefaultAuditor),
                Arguments.of("Unconfigured default key generator", unconfiguredDefaultKeyGenerator),
                Arguments.of("Unconfigured key generate strategies key generator", unconfiguredKeyGenerateStrategiesKeyGenerator),
                Arguments.of("Unsupported key generate strategies type", unsupportedKeyGenerateStrategies));
    }
    
    private static ShardingAutoTableRuleConfiguration createValidAutoTableRuleConfiguration() {
        return createAutoTableRuleConfiguration("foo_auto_tbl", new HintShardingStrategyConfiguration("foo_hint"));
    }
    
    private static ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final String logicTable, final HintShardingStrategyConfiguration strategyConfig) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(logicTable, null);
        result.setShardingStrategy(strategyConfig);
        return result;
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final StandardShardingStrategyConfiguration strategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("foo_tbl", null);
        result.setDatabaseShardingStrategy(strategyConfig);
        return result;
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final ComplexShardingStrategyConfiguration strategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("foo_tbl", null);
        result.setTableShardingStrategy(strategyConfig);
        return result;
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final ShardingAuditStrategyConfiguration strategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("foo_tbl", null);
        result.setAuditStrategy(strategyConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createDefaultKeyGenerateStrategyRuleConfiguration(final KeyGenerateStrategyConfiguration strategyConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultKeyGenerateStrategy(strategyConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createKeyGenerateStrategiesRuleConfiguration(final String name,
                                                                                          final KeyGenerateStrategiesConfiguration strategyConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getKeyGenerateStrategies().put(name, strategyConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createShardingAlgorithmsRuleConfiguration(final String name, final AlgorithmConfiguration algorithmConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put(name, algorithmConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createKeyGeneratorsRuleConfiguration(final String name, final AlgorithmConfiguration algorithmConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getKeyGenerators().put(name, algorithmConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createAuditorsRuleConfiguration(final String name, final AlgorithmConfiguration algorithmConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getAuditors().put(name, algorithmConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createRuleConfiguration(final ShardingTableRuleConfiguration tableConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(tableConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createRuleConfiguration(final ShardingAutoTableRuleConfiguration tableConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getAutoTables().add(tableConfig);
        return result;
    }
    
    private static ShardingRuleConfiguration createRuleConfiguration(final ShardingTableReferenceRuleConfiguration tableGroupConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getBindingTableGroups().add(tableGroupConfig);
        return result;
    }
    
    private static final class UnsupportedKeyGenerateStrategiesConfiguration implements KeyGenerateStrategiesConfiguration {
        
        @Override
        public String getKeyGenerateType() {
            return "unsupported";
        }
        
        @Override
        public String getKeyGeneratorName() {
            return "foo_key_generator";
        }
    }
}
