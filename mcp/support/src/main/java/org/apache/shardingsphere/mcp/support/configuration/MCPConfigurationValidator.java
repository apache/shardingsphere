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

package org.apache.shardingsphere.mcp.support.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MCP configuration validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPConfigurationValidator {
    
    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure().messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory().getValidator();
    
    /**
     * Validate MCP configuration.
     *
     * @param config configuration
     * @param configName configuration name
     */
    public static void validate(final Object config, final String configName) {
        ShardingSpherePreconditions.checkNotNull(config, () -> new IllegalArgumentException(String.format("%s cannot be null.", configName)));
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(config);
        ShardingSpherePreconditions.checkMustEmpty(violations, () -> new IllegalArgumentException(violations.stream()
                .map(each -> formatViolation(configName, each)).sorted().collect(Collectors.joining("; "))));
    }
    
    private static String formatViolation(final String configName, final ConstraintViolation<Object> violation) {
        if (violation.getPropertyPath().toString().isEmpty()) {
            return violation.getMessage();
        }
        return String.format("%s property `%s` %s.", configName, violation.getPropertyPath(), violation.getMessage());
    }
}
