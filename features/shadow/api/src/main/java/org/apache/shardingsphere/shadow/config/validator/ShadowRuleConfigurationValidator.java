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

package org.apache.shardingsphere.shadow.config.validator;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Shadow rule configuration validator.
 */
public final class ShadowRuleConfigurationValidator implements ConstraintValidator<ValidShadowRuleConfiguration, ShadowRuleConfiguration> {
    
    @Override
    public boolean isValid(final ShadowRuleConfiguration value, final ConstraintValidatorContext context) {
        return isDefaultShadowAlgorithmValid(value, context) && areTableDataSourcesValid(value, context) && areTableAlgorithmsValid(value, context);
    }
    
    private boolean isDefaultShadowAlgorithmValid(final ShadowRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (null == ruleConfig.getDefaultShadowAlgorithmName()) {
            return true;
        }
        AlgorithmConfiguration algorithmConfig = ruleConfig.getShadowAlgorithms().get(ruleConfig.getDefaultShadowAlgorithmName());
        if (null != algorithmConfig && "SQL_HINT".equals(algorithmConfig.getType())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("must reference a configured SQL_HINT shadow algorithm").addPropertyNode("defaultShadowAlgorithmName").addConstraintViolation();
        return false;
    }
    
    private boolean areTableDataSourcesValid(final ShadowRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        Collection<String> dataSourceNames = ruleConfig.getDataSources().stream().map(ShadowDataSourceConfiguration::getName).collect(Collectors.toSet());
        return ruleConfig.getTables().entrySet().stream().allMatch(entry -> areTableDataSourcesValid(entry.getKey(), entry.getValue(), dataSourceNames, context));
    }
    
    private boolean areTableDataSourcesValid(final String tableName, final ShadowTableConfiguration tableConfig, final Collection<String> dataSourceNames, final ConstraintValidatorContext context) {
        for (String each : tableConfig.getDataSourceNames()) {
            if (!dataSourceNames.contains(each)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(String.format("table `%s` references unconfigured shadow data source `%s`", tableName, each))
                        .addPropertyNode("tables").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
    
    private boolean areTableAlgorithmsValid(final ShadowRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        return ruleConfig.getTables().entrySet().stream().allMatch(entry -> areTableAlgorithmsValid(entry.getKey(), entry.getValue(), ruleConfig, context));
    }
    
    private boolean areTableAlgorithmsValid(final String tableName, final ShadowTableConfiguration tableConfig, final ShadowRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        for (String each : tableConfig.getShadowAlgorithmNames()) {
            if (!ruleConfig.getShadowAlgorithms().containsKey(each)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(String.format("table `%s` references unconfigured shadow algorithm `%s`", tableName, each))
                        .addPropertyNode("tables").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
