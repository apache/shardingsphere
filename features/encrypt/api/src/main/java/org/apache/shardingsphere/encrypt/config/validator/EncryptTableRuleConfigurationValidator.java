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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Optional;

/**
 * Encrypt table rule configuration validator.
 */
public final class EncryptTableRuleConfigurationValidator implements ConstraintValidator<ValidEncryptTableRuleConfiguration, EncryptTableRuleConfiguration> {
    
    @Override
    public boolean isValid(final EncryptTableRuleConfiguration value, final ConstraintValidatorContext context) {
        Collection<String> logicColumnNames = new CaseInsensitiveSet<>(value.getColumns().size());
        for (EncryptColumnRuleConfiguration each : value.getColumns()) {
            logicColumnNames.add(each.getName());
        }
        return value.getColumns().stream().allMatch(each -> isDerivedColumnValid(each, logicColumnNames, context));
    }
    
    private boolean isDerivedColumnValid(final EncryptColumnRuleConfiguration columnConfig, final Collection<String> logicColumnNames, final ConstraintValidatorContext context) {
        Optional<EncryptColumnItemRuleConfiguration> assistedQuery = columnConfig.getAssistedQuery();
        if (assistedQuery.isPresent() && logicColumnNames.contains(assistedQuery.get().getName())) {
            return addConstraintViolation("assisted query", assistedQuery.get().getName(), context);
        }
        Optional<EncryptColumnItemRuleConfiguration> likeQuery = columnConfig.getLikeQuery();
        if (likeQuery.isPresent() && logicColumnNames.contains(likeQuery.get().getName())) {
            return addConstraintViolation("like query", likeQuery.get().getName(), context);
        }
        return true;
    }
    
    private boolean addConstraintViolation(final String columnType, final String columnName, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.format("%s column `%s` conflicts with a logic column", columnType, columnName)).addPropertyNode("columns").addConstraintViolation();
        return false;
    }
}
