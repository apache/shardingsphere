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

package org.apache.shardingsphere.infra.hint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.props.TypedPropertyKey;

/**
 * Typed property key of SQL Hint.
 */
@RequiredArgsConstructor
@Getter
public enum SQLHintPropertiesKey implements TypedPropertyKey {
    
    /**
     * Whether hint route write data source or not.
     */
    WRITE_ROUTE_ONLY_KEY("WRITE_ROUTE_ONLY", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether to use traffic or not.
     */
    USE_TRAFFIC_KEY("USE_TRAFFIC", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Whether hint skip encrypt rewrite or not.
     */
    SKIP_ENCRYPT_REWRITE_KEY("SKIP_ENCRYPT_REWRITE", String.valueOf(Boolean.FALSE), boolean.class),
    
    /**
     * Hint disable audit names.
     */
    DISABLE_AUDIT_NAMES_KEY("DISABLE_AUDIT_NAMES", "", String.class),
    
    /**
     * Hint sharding database value.
     */
    SHARDING_DATABASE_VALUE_KEY("SHARDING_DATABASE_VALUE", "", Comparable.class),
    
    /**
     * Hint sharding table value.
     */
    SHARDING_TABLE_VALUE_KEY("SHARDING_TABLE_VALUE", "", Comparable.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
