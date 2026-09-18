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

package org.apache.shardingsphere.infra.config.rule.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule configuration validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleConfigurationValidator {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    /**
     * Validate rule configurations.
     *
     * @param ruleConfigs rule configurations
     */
    public static void validate(final Collection<RuleConfiguration> ruleConfigs) {
        for (RuleConfiguration each : ruleConfigs) {
            validate(each);
        }
    }
    
    /**
     * Validate rule configuration.
     *
     * @param ruleConfig rule configuration
     */
    public static void validate(final RuleConfiguration ruleConfig) {
        validate(ruleConfig, ruleConfig.getClass().getSimpleName());
    }
    
    private static void validate(final Object config, final String ruleType) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(config);
        ShardingSpherePreconditions.checkMustEmpty(violations, () -> new InvalidRuleConfigurationException(
                ruleType, violations.stream().map(RuleConfigurationValidator::formatViolation).sorted().collect(Collectors.joining("; "))));
    }
    
    /**
     * Validate rule item configuration.
     *
     * @param ruleConfig owner rule configuration
     * @param ruleItemConfig rule item configuration
     */
    public static void validateRuleItem(final RuleConfiguration ruleConfig, final Object ruleItemConfig) {
        validate(ruleItemConfig, ruleConfig.getClass().getSimpleName());
    }
    
    private static String formatViolation(final ConstraintViolation<?> violation) {
        if (violation.getPropertyPath().toString().isEmpty()) {
            return violation.getMessage();
        }
        String message = violation.getMessage();
        return String.format("Property `%s` %s%s", violation.getPropertyPath(), message, message.endsWith(".") ? "" : ".");
    }
}
