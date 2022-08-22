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

package org.apache.shardingsphere.infra.binder;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;

import java.util.List;
import java.util.Optional;

/**
 * Query context.
 */
@Getter
public final class QueryContext {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private String sqlStatementDatabaseName;
    
    public QueryContext(final SQLStatementContext<?> sqlStatementContext, final String sql, final List<Object> parameters) {
        this.sqlStatementContext = sqlStatementContext;
        this.sql = sql;
        this.parameters = parameters;
        if (sqlStatementContext instanceof TableAvailable) {
            ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().ifPresent(optional -> sqlStatementDatabaseName = optional);
        }
    }
    
    /**
     * Find sql statement database name.
     * 
     * @return database name
     */
    public Optional<String> findSqlStatementDatabaseName() {
        return Optional.ofNullable(sqlStatementDatabaseName);
    }
}
