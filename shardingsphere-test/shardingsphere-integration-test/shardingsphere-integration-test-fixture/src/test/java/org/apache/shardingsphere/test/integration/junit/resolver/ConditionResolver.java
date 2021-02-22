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
import org.apache.shardingsphere.test.integration.junit.annotation.Conditional;
import org.junit.runners.model.Annotatable;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public class ConditionResolver {
    
    private final TestClass testClass;
    
    /**
     * Predicate whether the case/field/method is ignored.
     *
     * @param annotatable test case/field/method
     * @return ignored if false
     */
    public boolean filter(final Annotatable annotatable) {
        return Arrays.stream(annotatable.getAnnotations())
                .filter(e -> e.annotationType().isAnnotationPresent(Conditional.class))
                .map(e -> {
                    Conditional annotation = annotatable.getAnnotation(Conditional.class);
                    if (Objects.isNull(annotation)) {
                        return true;
                    }
                    return invokeExplosively(annotation, e);
                }).reduce((a, b) -> a && b)
                .orElse(true);
    }
    
    @SuppressWarnings("unchecked")
    private boolean invokeExplosively(final Conditional conditional, final Annotation condition) {
        try {
            return conditional.value().newInstance().matches(condition);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            throw new RuntimeException("Failed to instantiate conditional " + conditional.value() + ".");
        }
    }
}
