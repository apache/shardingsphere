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

package org.apache.shardingsphere.sql.parser.statement.core.statement.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * SQL statement attribute.
 */
public final class SQLStatementAttributes {
    
    private final Collection<SQLStatementAttribute> attributes;
    
    public SQLStatementAttributes(final SQLStatementAttribute... attributes) {
        this.attributes = Arrays.asList(attributes);
    }
    
    /**
     * Find SQL statement attribute.
     *
     * @param attributeClass SQL statement attribute class
     * @param <T> type of SQL statement attribute
     * @return found SQL statement attribute
     */
    @SuppressWarnings("unchecked")
    public <T extends SQLStatementAttribute> Optional<T> findAttribute(final Class<T> attributeClass) {
        for (SQLStatementAttribute each : attributes) {
            if (attributeClass.isAssignableFrom(each.getClass())) {
                return Optional.of((T) each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get SQL statement attribute.
     *
     * @param attributeClass SQL statement attribute class
     * @param <T> type of SQL statement attribute
     * @return got SQL statement attribute
     */
    public <T extends SQLStatementAttribute> T getAttribute(final Class<T> attributeClass) {
        return findAttribute(attributeClass).orElseThrow(() -> new IllegalStateException(String.format("Can not find SQL statement attribute: %s", attributeClass)));
    }
}
