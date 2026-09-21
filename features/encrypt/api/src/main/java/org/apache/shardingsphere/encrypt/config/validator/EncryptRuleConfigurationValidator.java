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

package org.apache.shardingsphere.encrypt.config.validator;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Encrypt rule configuration validator.
 */
public final class EncryptRuleConfigurationValidator implements ConstraintValidator<ValidEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    @Override
    public boolean isValid(final EncryptRuleConfiguration value, final ConstraintValidatorContext context) {
        return value.getTables().stream().allMatch(each -> isTableValid(each, value, context));
    }
    
    private boolean isTableValid(final EncryptTableRuleConfiguration tableConfig, final EncryptRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        return tableConfig.getColumns().stream().allMatch(each -> isColumnValid(each, ruleConfig, context));
    }
    
    private boolean isColumnValid(final EncryptColumnRuleConfiguration columnConfig, final EncryptRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (!isEncryptorReferenceValid(columnConfig.getCipher(), ruleConfig, context)) {
            return false;
        }
        if (columnConfig.getAssistedQuery().isPresent() && !isEncryptorReferenceValid(columnConfig.getAssistedQuery().get(), ruleConfig, context)) {
            return false;
        }
        return !columnConfig.getLikeQuery().isPresent() || isEncryptorReferenceValid(columnConfig.getLikeQuery().get(), ruleConfig, context);
    }
    
    private boolean isEncryptorReferenceValid(final EncryptColumnItemRuleConfiguration columnItemConfig, final EncryptRuleConfiguration ruleConfig, final ConstraintValidatorContext context) {
        if (ruleConfig.getEncryptors().containsKey(columnItemConfig.getEncryptorName())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.format("references unconfigured encryptor `%s`", columnItemConfig.getEncryptorName())).addPropertyNode("tables").addConstraintViolation();
        return false;
    }
}
