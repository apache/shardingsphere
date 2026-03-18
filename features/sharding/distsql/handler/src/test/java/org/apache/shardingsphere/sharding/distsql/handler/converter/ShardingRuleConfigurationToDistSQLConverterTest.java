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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRuleConfigurationToDistSQLConverterTest {
    
    @SuppressWarnings("unchecked")
    private final RuleConfigurationToDistSQLConverter<ShardingRuleConfiguration> converter = TypedSPILoader.getService(RuleConfigurationToDistSQLConverter.class, ShardingRuleConfiguration.class);
    
    @Test
    void assertConvertWithEmptyTables() {
        ShardingRuleConfiguration shardingRuleConfig = mock(ShardingRuleConfiguration.class);
        when(shardingRuleConfig.getTables()).thenReturn(Collections.emptyList());
        when(shardingRuleConfig.getAutoTables()).thenReturn(Collections.emptyList());
        assertThat(converter.convert(shardingRuleConfig), is(""));
    }
    
    @Test
    void assertConvert() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createShardingTableRuleConfiguration());
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        shardingRuleConfig.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getShardingAlgorithms().put("database_inline", createShardingInlineAlgorithmConfiguration("ds_${user_id % 2}"));
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", createShardingInlineAlgorithmConfiguration("t_order_${order_id % 2}"));
        shardingRuleConfig.getKeyGenerators().put("snowflake", createKeyGeneratorConfiguration());
        shardingRuleConfig.getAuditors().put("sharding_key_required_auditor", createAuditorConfiguration());
        assertThat(converter.convert(shardingRuleConfig),
                is("CREATE SHARDING TABLE RULE t_order (" + System.lineSeparator() + "DATANODES('ds_${0..1}.t_order_${0..1}')," + System.lineSeparator()
                        + "TABLE_STRATEGY(TYPE='standard', SHARDING_COLUMN=order_id, SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))),"
                        + System.lineSeparator() + "KEY_GENERATE_STRATEGY(COLUMN=order_id, TYPE(NAME='snowflake'))," + System.lineSeparator()
                        + "AUDIT_STRATEGY(TYPE(NAME='dml_sharding_conditions'), ALLOW_HINT_DISABLE=true)" + System.lineSeparator() + ");" + System.lineSeparator() + System.lineSeparator()
                        + "CREATE DEFAULT SHARDING DATABASE STRATEGY(TYPE='standard', SHARDING_COLUMN=user_id, "
                        + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='ds_${user_id % 2}'))));" + System.lineSeparator() + System.lineSeparator()
                        + "CREATE DEFAULT SHARDING TABLE STRATEGY(TYPE='none');"));
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_inline"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("sharding_key_required_auditor"), true));
        return result;
    }
    
    private AlgorithmConfiguration createShardingInlineAlgorithmConfiguration(final String algorithmExpression) {
        return new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", algorithmExpression)));
    }
    
    private AlgorithmConfiguration createKeyGeneratorConfiguration() {
        return new AlgorithmConfiguration("SNOWFLAKE", new Properties());
    }
    
    private AlgorithmConfiguration createAuditorConfiguration() {
        return new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties());
    }
}
