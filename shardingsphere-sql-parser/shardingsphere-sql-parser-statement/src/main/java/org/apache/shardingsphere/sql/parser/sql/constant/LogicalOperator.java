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

package org.apache.shardingsphere.sql.parser.sql.constant;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Logical operator.
 */
public enum LogicalOperator {
    
    AND("AND", "&&"), 
    OR("OR", "||");
    
    private final Collection<String> texts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    LogicalOperator(final String... texts) {
        this.texts.addAll(Arrays.asList(texts));
    }
    
    /**
     * Get logical operator value from text.
     *
     * @param text text
     * @return logical operator value
     */
    public static Optional<LogicalOperator> valueFrom(final String text) {
        for (LogicalOperator each : values()) {
            if (each.texts.contains(text)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
