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

package org.apache.shardingsphere.infra.binder.statement;

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

/**
 * SQL statement context.
 * 
 * @param <T> type of SQL statement
 */
public interface SQLStatementContext<T extends SQLStatement> {
    
    /**
     * Get SQL statement.
     * 
     * @return SQL statement
     */
    T getSqlStatement();
    
    /**
     * Get tables context.
     *
     * @return tables context
     */
    TablesContext getTablesContext();
    
    /**
     * Determine whether SQL is read-only.
     *
     * @return true if read-only, otherwise false
     */
    default boolean isReadOnly() {
        return SQLUtil.isReadOnly(getSqlStatement());
    }
}
