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

package org.apache.shardingsphere.sql.parser.statement.core.enums;

import com.cedarsoftware.util.CaseInsensitiveMap;

import java.util.Map;
import java.util.Optional;

/**
 * Logical operator.
 */
public enum LogicalOperator {
    
    AND, OR, NOT, XOR;
    
    private static final Map<String, LogicalOperator> LOGICAL_OPERATORS = new CaseInsensitiveMap<>(6, 1F);
    
    static {
        LOGICAL_OPERATORS.put("AND", AND);
        LOGICAL_OPERATORS.put("&&", AND);
        LOGICAL_OPERATORS.put("OR", OR);
        LOGICAL_OPERATORS.put("||", OR);
        LOGICAL_OPERATORS.put("!", NOT);
        LOGICAL_OPERATORS.put("XOR", XOR);
    }
    
    /**
     * Get logical operator value from text.
     *
     * @param text text
     * @return logical operator value
     */
    public static Optional<LogicalOperator> valueFrom(final String text) {
        return Optional.ofNullable(LOGICAL_OPERATORS.get(text));
    }
}
