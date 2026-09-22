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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.reference;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraint for names that reference configurations in a map on the same rule configuration.
 * Property paths use public getters. Collections are traversed, and optional values are unwrapped.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConfigurationReferenceExistsValidator.class)
@Documented
public @interface ConfigurationReferenceExists {
    
    /**
     * Get reference property paths from the annotated object.
     *
     * @return reference property paths
     */
    String[] referencePaths();
    
    /**
     * Get the name of the map property containing configured values.
     *
     * @return configuration pool property name
     */
    String pool();
    
    /**
     * Whether an empty reference name is allowed.
     *
     * @return whether an empty reference name is allowed
     */
    boolean allowEmpty() default false;
    
    /**
     * Get the violation message. Use {pool} for the configuration pool property and {reference} for the missing name.
     *
     * @return violation message
     */
    String message() default "references unconfigured {pool} `{reference}`";
    
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
}
