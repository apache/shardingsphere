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

package org.apache.shardingsphere.infra.binder.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.MoveStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Move statement context.
 */
@Getter
public final class MoveStatementContext extends CommonSQLStatementContext<MoveStatement> implements CursorAvailable, WhereAvailable, CursorDefinitionAware {
    
    private CursorStatementContext cursorStatementContext;
    
    private TablesContext tablesContext;
    
    public MoveStatementContext(final MoveStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(Collections.emptyList(), getDatabaseType());
    }
    
    @Override
    public Optional<CursorNameSegment> getCursorName() {
        return Optional.of(getSqlStatement().getCursorName());
    }
    
    @Override
    public void setUpCursorDefinition(final CursorStatementContext cursorStatementContext) {
        this.cursorStatementContext = cursorStatementContext;
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(cursorStatementContext.getSqlStatement().getSelect());
        tablesContext = new TablesContext(tableExtractor.getRewriteTables(), getDatabaseType());
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return null != cursorStatementContext ? cursorStatementContext.getWhereSegments() : Collections.emptyList();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return null != cursorStatementContext ? cursorStatementContext.getColumnSegments() : Collections.emptyList();
    }
}
