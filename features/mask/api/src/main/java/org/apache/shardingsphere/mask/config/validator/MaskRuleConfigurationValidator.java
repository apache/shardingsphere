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

package org.apache.shardingsphere.mask.config.validator;

import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Mask rule configuration validator.
 */
public final class MaskRuleConfigurationValidator implements ConstraintValidator<ValidMaskRuleConfiguration, MaskRuleConfiguration> {
    
    @Override
    public boolean isValid(final MaskRuleConfiguration value, final ConstraintValidatorContext context) {
        return value.getTables().stream().allMatch(each -> isTableValid(each, value, context));
    }
    
    private boolean isTableValid(final MaskTableRuleConfiguration tableConfig, final MaskRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        return tableConfig.getColumns().stream().allMatch(each -> isColumnValid(each, ruleConfig, context));
    }
    
    private boolean isColumnValid(final MaskColumnRuleConfiguration columnConfig, final MaskRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (ruleConfig.getMaskAlgorithms().containsKey(columnConfig.getMaskAlgorithm())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.format("references unconfigured mask algorithm `%s`", columnConfig.getMaskAlgorithm())).addPropertyNode("tables").addConstraintViolation();
        return false;
    }
}
