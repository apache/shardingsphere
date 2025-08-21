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

package org.apache.shardingsphere.database.connector.core.metadata.database.enums;

/**
 * Nulls order type.
 */
public enum NullsOrderType {
    
    /**
     * Nulls first for DESC, nulls last for ASC.
     */
    HIGH,
    
    /**
     * Nulls last for DESC, nulls first for ASC.
     */
    LOW,
    
    /**
     * Nulls first.
     */
    FIRST,
    
    /**
     * Nulls last.
     */
    LAST;
    
    /**
     * Get resolved order type.
     *
     * @param orderDirection order direction
     * @return resolved order type
     */
    public NullsOrderType getResolvedOrderType(final String orderDirection) {
        if (HIGH == this) {
            return "DESC".equalsIgnoreCase(orderDirection) ? FIRST : LAST;
        }
        if (LOW == this) {
            return "DESC".equalsIgnoreCase(orderDirection) ? LAST : FIRST;
        }
        return this;
    }
}
