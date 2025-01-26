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
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Alter view statement context.
 */
@Getter
public final class AlterViewStatementContext extends CommonSQLStatementContext implements TableAvailable, WhereAvailable {
    
    private final TablesContext tablesContext;
    
    private final SelectStatementContext selectStatementContext;
    
    public AlterViewStatementContext(final ShardingSphereMetaData metaData, final List<Object> params, final AlterViewStatement sqlStatement, final String currentDatabaseName) {
        super(sqlStatement);
        Collection<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(sqlStatement.getView());
        Optional<SelectStatement> selectStatement = sqlStatement.getSelect();
        selectStatement.ifPresent(optional -> extractTables(optional, tables));
        sqlStatement.getRenameView().ifPresent(tables::add);
        tablesContext = new TablesContext(tables);
        selectStatementContext = selectStatement.map(optional -> createSelectStatementContext(metaData, params, optional, currentDatabaseName)).orElse(null);
    }
    
    private SelectStatementContext createSelectStatementContext(final ShardingSphereMetaData metaData, final List<Object> params, final SelectStatement selectStatement,
                                                                final String currentDatabaseName) {
        SelectStatementContext result = new SelectStatementContext(metaData, params, selectStatement, currentDatabaseName, Collections.emptyList());
        result.setSubqueryType(SubqueryType.VIEW_DEFINITION);
        return result;
    }
    
    private void extractTables(final SelectStatement selectStatement, final Collection<SimpleTableSegment> tables) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(selectStatement);
        tables.addAll(extractor.getRewriteTables());
    }
    
    /**
     * Get select statement context.
     *
     * @return select statement context
     */
    public Optional<SelectStatementContext> getSelectStatementContext() {
        return Optional.ofNullable(selectStatementContext);
    }
    
    @Override
    public AlterViewStatement getSqlStatement() {
        return (AlterViewStatement) super.getSqlStatement();
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return getSelectStatementContext().isPresent() ? getSelectStatementContext().get().getWhereSegments() : Collections.emptyList();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return getSelectStatementContext().isPresent() ? getSelectStatementContext().get().getColumnSegments() : Collections.emptyList();
    }
    
    @Override
    public Collection<BinaryOperationExpression> getJoinConditions() {
        return getSelectStatementContext().isPresent() ? getSelectStatementContext().get().getJoinConditions() : Collections.emptyList();
    }
}
