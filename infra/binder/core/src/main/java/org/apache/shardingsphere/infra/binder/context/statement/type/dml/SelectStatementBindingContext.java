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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine.PaginationContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.List;

/**
 * Select SQL statement binding context.
 */
public final class SelectStatementBindingContext implements SQLStatementContext {
    
    private final SelectStatementBaseContext baseContext;
    
    @Getter
    private final PaginationContext paginationContext;
    
    public SelectStatementBindingContext(final List<Object> params, final SelectStatementBaseContext baseContext) {
        this.baseContext = baseContext;
        DialectPaginationOption paginationOption = new DatabaseTypeRegistry(baseContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getPaginationOption();
        paginationContext =
                new PaginationContextEngine(paginationOption).createPaginationContext(baseContext.getSqlStatement(), baseContext.getProjectionsContext(), params, baseContext.getWhereSegments());
    }
    
    @Override
    public SelectStatement getSqlStatement() {
        return baseContext.getSqlStatement();
    }
    
    @Override
    public TablesContext getTablesContext() {
        return baseContext.getTablesContext();
    }
}
