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

package org.apache.shardingsphere.sql.parser.sql.common.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Logical operator.
 */
public enum LogicalOperator {
    
    AND, OR;
    
    private static final Map<String, LogicalOperator> MAPS = new HashMap<>(16, 1);
    
    static {
        MAPS.put("and", AND);
        MAPS.put("And", AND);
        MAPS.put("aNd", AND);
        MAPS.put("anD", AND);
        MAPS.put("ANd", AND);
        MAPS.put("AnD", AND);
        MAPS.put("aND", AND);
        MAPS.put("AND", AND);
        MAPS.put("&&", AND);
        MAPS.put("or", OR);
        MAPS.put("Or", OR);
        MAPS.put("oR", OR);
        MAPS.put("OR", OR);
        MAPS.put("||", OR);
    }
    
    /**
     * Get logical operator value from text.
     *
     * @param text text
     * @return logical operator value
     */
    public static Optional<LogicalOperator> valueFrom(final String text) {
        return Optional.ofNullable(MAPS.get(text));
    }
}
