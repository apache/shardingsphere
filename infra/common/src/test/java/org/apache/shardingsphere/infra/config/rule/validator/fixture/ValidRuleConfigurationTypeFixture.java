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

package org.apache.shardingsphere.infra.config.rule.validator.fixture;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valid rule configuration type fixture constraint.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRuleConfigurationTypeFixture.Validator.class)
@Documented
public @interface ValidRuleConfigurationTypeFixture {
    
    /**
     * Get message.
     *
     * @return message
     */
    String message() default "contains invalid type configuration";
    
    /**
     * Get groups.
     *
     * @return groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Get payload.
     *
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Valid rule configuration type fixture validator.
     */
    final class Validator implements ConstraintValidator<ValidRuleConfigurationTypeFixture, Object> {
        
        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            return false;
        }
    }
}
