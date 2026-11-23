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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sql;

import java.util.Optional;

/**
 * Dialect SQL option.
 */
public interface DialectSQLOption {
    
    /**
     * Judge whether whole-row projection is supported.
     *
     * @return whether whole-row projection is supported
     */
    default boolean isSupportWholeRowProjection() {
        return false;
    }
    
    /**
     * Get dummy table name.
     *
     * @return dummy table name, or empty if not declared
     */
    default Optional<String> getDummyTableName() {
        return Optional.empty();
    }
    
    /**
     * Get the alias exposing the proposed row in an insert conflict action.
     *
     * @return proposed row alias, or empty if unsupported
     */
    default Optional<String> getInsertConflictProposedRowAlias() {
        return Optional.empty();
    }
}
