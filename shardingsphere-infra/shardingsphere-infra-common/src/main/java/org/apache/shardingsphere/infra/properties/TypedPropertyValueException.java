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

/**
 * Typed property value exception.
 */
public final class TypedPropertyValueException extends Exception {
    
    private static final long serialVersionUID = -2989212435757964906L;
    
    public TypedPropertyValueException(final TypedPropertyKey key, final String value) {
        super(String.format("Value `%s` of `%s` cannot convert to type `%s`.", value, key.getKey(), key.getType().getName()));
    }
}
