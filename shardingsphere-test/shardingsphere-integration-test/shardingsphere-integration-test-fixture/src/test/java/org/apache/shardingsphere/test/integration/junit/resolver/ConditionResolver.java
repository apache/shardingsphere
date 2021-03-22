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

package org.apache.shardingsphere.test.integration.junit.resolver;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.integration.junit.annotation.ShardingSphereITConditional;
import org.junit.runners.model.Annotatable;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@RequiredArgsConstructor
public class ConditionResolver {
    
    /**
     * Predicate whether the case/field/method is ignored.
     *
     * @param annotatable test case/field/method
     * @return ignored if false
     */
    public boolean filter(final Annotatable annotatable) {
        return Arrays.stream(annotatable.getAnnotations())
                .filter(e -> e.annotationType().isAnnotationPresent(ShardingSphereITConditional.class))
                .map(e -> {
                    ShardingSphereITConditional conditional = e.annotationType().getAnnotation(ShardingSphereITConditional.class);
                    return invokeExplosively(conditional, e);
                })
                .reduce((a, b) -> a && b)
                .orElse(true);
    }
    
    @SuppressWarnings("unchecked")
    private boolean invokeExplosively(final ShardingSphereITConditional conditional, final Annotation annotation) {
        try {
            return conditional.value().newInstance().matches(annotation);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            throw new RuntimeException("Failed to instantiate conditional " + conditional.value() + ".");
        }
    }
}
