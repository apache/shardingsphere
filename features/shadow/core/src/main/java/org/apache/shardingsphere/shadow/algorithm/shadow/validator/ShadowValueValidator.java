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

package org.apache.shardingsphere.shadow.algorithm.shadow.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.exception.data.UnsupportedShadowColumnTypeException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Shadow value validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowValueValidator {
    
    private static final Set<Class<?>> UNSUPPORTED_TYPES = new HashSet<>();
    
    static {
        UNSUPPORTED_TYPES.add(Date.class);
        UNSUPPORTED_TYPES.add(Enum.class);
    }
    
    /**
     * Validate shadow value.
     *
     * @param table table name
     * @param column column name
     * @param shadowValue shadow value
     * @throws UnsupportedShadowColumnTypeException unsupported shadow column type exception
     */
    public static void validate(final String table, final String column, final Comparable<?> shadowValue) {
        for (Class<?> each : UNSUPPORTED_TYPES) {
            if (each.isAssignableFrom(shadowValue.getClass())) {
                throw new UnsupportedShadowColumnTypeException(table, column, each);
            }
        }
    }
}
