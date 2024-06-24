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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.MoveStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Move statement context.
 */
@Getter
public final class MoveStatementContext extends CommonSQLStatementContext implements CursorAvailable, TableAvailable, WhereAvailable, CursorDefinitionAware {
    
    private CursorStatementContext cursorStatementContext;
    
    private TablesContext tablesContext;
    
    public MoveStatementContext(final MoveStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(Collections.emptyList(), getDatabaseType());
    }
    
    @Override
    public MoveStatement getSqlStatement() {
        return (MoveStatement) super.getSqlStatement();
    }
    
    @Override
    public Optional<CursorNameSegment> getCursorName() {
        return Optional.of(getSqlStatement().getCursorName());
    }
    
    @Override
    public void setUpCursorDefinition(final CursorStatementContext cursorStatementContext) {
        this.cursorStatementContext = cursorStatementContext;
        tablesContext = cursorStatementContext.getTablesContext();
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return null == cursorStatementContext ? Collections.emptyList() : cursorStatementContext.getWhereSegments();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return null == cursorStatementContext ? Collections.emptyList() : cursorStatementContext.getColumnSegments();
    }
    
    @Override
    public Collection<BinaryOperationExpression> getJoinConditions() {
        return null == cursorStatementContext ? Collections.emptyList() : cursorStatementContext.getJoinConditions();
    }
}
