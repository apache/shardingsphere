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

package org.apache.shardingsphere.infra.properties.fixture;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.properties.TypedPropertyKey;

@RequiredArgsConstructor
@Getter
public enum TestTypedPropertyKey implements TypedPropertyKey {
    
    BOOLEAN_VALUE("boolean", String.valueOf(Boolean.FALSE), boolean.class),
    
    BOOLEAN_OBJECT_VALUE("Boolean", String.valueOf(Boolean.FALSE), Boolean.class),
    
    INT_VALUE("int", "10", int.class),
    
    INT_OBJECT_VALUE("Integer", "10", Integer.class),
    
    LONG_VALUE("long", "1000", long.class),
    
    LONG_OBJECT_VALUE("Long", "1000", Long.class),
    
    STRING_VALUE("String", "value", String.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
