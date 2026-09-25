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

package org.apache.shardingsphere.sharding.api.config.validator;

import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Sharding rule configuration validator.
 */
public final class ShardingRuleConfigurationValidator implements ConstraintValidator<ValidShardingRuleConfiguration, ShardingRuleConfiguration> {
    
    @Override
    public boolean isValid(final ShardingRuleConfiguration value, final ConstraintValidatorContext context) {
        return areTablesValid(value, context)
                && areAutoTablesValid(value, context)
                && isKeyGenerateStrategyValid(value.getDefaultKeyGenerateStrategy(), "defaultKeyGenerateStrategy", value, context)
                && areKeyGenerateStrategiesValid(value, context)
                && isAuditStrategyValid(value.getDefaultAuditStrategy(), "defaultAuditStrategy", value, context)
                && isShardingStrategyValid(value.getDefaultDatabaseShardingStrategy(), "defaultDatabaseShardingStrategy", value, context)
                && isShardingStrategyValid(value.getDefaultTableShardingStrategy(), "defaultTableShardingStrategy", value, context);
    }
    
    private boolean areTablesValid(final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        return ruleConfig.getTables().stream().noneMatch(each -> !isAuditStrategyValid(each.getAuditStrategy(), "tables", ruleConfig, context)
                || !isShardingStrategyValid(each.getDatabaseShardingStrategy(), "tables", ruleConfig, context)
                || !isShardingStrategyValid(each.getTableShardingStrategy(), "tables", ruleConfig, context));
    }
    
    private boolean areAutoTablesValid(final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        return ruleConfig.getAutoTables().stream().noneMatch(each -> !isAuditStrategyValid(each.getAuditStrategy(), "autoTables", ruleConfig, context)
                || !isAutoTableShardingStrategyValid(each.getShardingStrategy(), context)
                || !isShardingStrategyValid(each.getShardingStrategy(), "autoTables", ruleConfig, context));
    }
    
    private boolean isAutoTableShardingStrategyValid(final ShardingStrategyConfiguration strategyConfig, final ConstraintValidatorContext context) {
        return !(strategyConfig instanceof NoneShardingStrategyConfiguration) || addViolation("autoTables", "requires a sharding strategy other than `none`", context);
    }
    
    private boolean isKeyGenerateStrategyValid(final KeyGenerateStrategyConfiguration strategyConfig, final String propertyName,
                                               final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (null == strategyConfig || ruleConfig.getKeyGenerators().containsKey(strategyConfig.getKeyGeneratorName())) {
            return true;
        }
        return addViolation(propertyName, String.format("references unconfigured key generator `%s`", strategyConfig.getKeyGeneratorName()), context);
    }
    
    private boolean areKeyGenerateStrategiesValid(final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        return ruleConfig.getKeyGenerateStrategies().entrySet().stream().allMatch(entry -> isKeyGenerateStrategiesValid(entry.getKey(), entry.getValue(), ruleConfig, context));
    }
    
    private boolean isKeyGenerateStrategiesValid(final String strategyName, final KeyGenerateStrategiesConfiguration strategyConfig,
                                                 final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (!(strategyConfig instanceof ColumnKeyGenerateStrategiesRuleConfiguration) && !(strategyConfig instanceof SequenceKeyGenerateStrategiesRuleConfiguration)) {
            return addViolation("keyGenerateStrategies", String.format("strategy `%s` must use column or sequence key generation", strategyName), context);
        }
        if (!ruleConfig.getKeyGenerators().containsKey(strategyConfig.getKeyGeneratorName())) {
            return addViolation("keyGenerateStrategies", String.format("strategy `%s` references unconfigured key generator `%s`",
                    strategyName, strategyConfig.getKeyGeneratorName()), context);
        }
        return true;
    }
    
    private boolean isAuditStrategyValid(final ShardingAuditStrategyConfiguration strategyConfig, final String propertyName,
                                         final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (null == strategyConfig) {
            return true;
        }
        return strategyConfig.getAuditorNames().stream()
                .filter(each -> !ruleConfig.getAuditors().containsKey(each)).findFirst()
                .map(each -> addViolation(propertyName, String.format("references unconfigured auditor `%s`", each), context)).orElse(true);
    }
    
    private boolean isShardingStrategyValid(final ShardingStrategyConfiguration strategyConfig, final String propertyName,
                                            final ShardingRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (null == strategyConfig || strategyConfig instanceof NoneShardingStrategyConfiguration || ruleConfig.getShardingAlgorithms().containsKey(strategyConfig.getShardingAlgorithmName())) {
            return true;
        }
        return addViolation(propertyName, String.format("references unconfigured sharding algorithm `%s`", strategyConfig.getShardingAlgorithmName()), context);
    }
    
    private boolean addViolation(final String propertyName, final String message, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(propertyName).addConstraintViolation();
        return false;
    }
}
