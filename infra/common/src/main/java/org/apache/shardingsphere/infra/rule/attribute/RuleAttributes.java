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

package org.apache.shardingsphere.infra.rule.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Rule attribute.
 */
public final class RuleAttributes {
    
    private final Collection<RuleAttribute> attributes;
    
    public RuleAttributes(final RuleAttribute... attributes) {
        this.attributes = Arrays.asList(attributes);
    }
    
    /**
     * Find rule attribute.
     * 
     * @param attributeClass rule attribute class
     * @param <T> type of rule attribute
     * @return found rule attribute
     */
    @SuppressWarnings("unchecked")
    public <T extends RuleAttribute> Optional<T> findAttribute(final Class<T> attributeClass) {
        for (RuleAttribute each : attributes) {
            if (attributeClass.isAssignableFrom(each.getClass())) {
                return Optional.of((T) each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get rule attribute.
     *
     * @param attributeClass rule attribute class
     * @param <T> type of rule attribute
     * @return got rule attribute
     */
    public <T extends RuleAttribute> T getAttribute(final Class<T> attributeClass) {
        return findAttribute(attributeClass).orElseThrow(() -> new IllegalStateException(String.format("Can not find rule attribute: %s", attributeClass)));
    }
}
