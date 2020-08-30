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

package org.apache.shardingsphere.infra.properties;

import lombok.Getter;

/**
 * Typed property value.
 */
@Getter
public final class TypedPropertyValue {
    
    private final Object value;
    
    public TypedPropertyValue(final TypedPropertyKey key, final String value) throws TypedPropertyValueException {
        this.value = createTypedValue(key, value);
    }
    
    private Object createTypedValue(final TypedPropertyKey key, final String value) throws TypedPropertyValueException {
        if (boolean.class == key.getType() || Boolean.class == key.getType()) {
            return Boolean.valueOf(value);
        }
        if (int.class == key.getType() || Integer.class == key.getType()) {
            try {
                return Integer.valueOf(value);
            } catch (final NumberFormatException ex) {
                throw new TypedPropertyValueException(key, value);
            }
        }
        if (long.class == key.getType() || Long.class == key.getType()) {
            try {
                return Long.valueOf(value);
            } catch (final NumberFormatException ex) {
                throw new TypedPropertyValueException(key, value);
            }
        }
        return value;
    }
}
