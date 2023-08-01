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
import org.apache.shardingsphere.infra.binder.context.aware.CursorDefinition;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Cursor statement context.
 */
@Getter
public final class CursorStatementContext extends CommonSQLStatementContext implements CursorAvailable, TableAvailable, WhereAvailable, CursorDefinition {
    
    private final Collection<WhereSegment> whereSegments = new LinkedList<>();
    
    private final Collection<ColumnSegment> columnSegments = new LinkedList<>();
    
    private final TablesContext tablesContext;
    
    private final SelectStatementContext selectStatementContext;
    
    public CursorStatementContext(final ShardingSphereMetaData metaData, final List<Object> params,
                                  final OpenGaussCursorStatement sqlStatement, final String defaultDatabaseName) {
        super(sqlStatement);
        tablesContext = new TablesContext(getSimpleTableSegments(), getDatabaseType());
        extractWhereSegments(whereSegments, sqlStatement.getSelect());
        ColumnExtractor.extractColumnSegments(columnSegments, whereSegments);
        selectStatementContext = new SelectStatementContext(metaData, params, sqlStatement.getSelect(), defaultDatabaseName);
    }
    
    private Collection<SimpleTableSegment> getSimpleTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(getSqlStatement().getSelect());
        return tableExtractor.getRewriteTables();
    }
    
    private void extractWhereSegments(final Collection<WhereSegment> whereSegments, final SelectStatement select) {
        select.getWhere().ifPresent(whereSegments::add);
        whereSegments.addAll(WhereExtractUtils.getSubqueryWhereSegments(select));
        whereSegments.addAll(WhereExtractUtils.getJoinWhereSegments(select));
    }
    
    @Override
    public OpenGaussCursorStatement getSqlStatement() {
        return (OpenGaussCursorStatement) super.getSqlStatement();
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return tablesContext.getSimpleTableSegments();
    }
    
    @Override
    public Optional<CursorNameSegment> getCursorName() {
        return Optional.of(getSqlStatement().getCursorName());
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return whereSegments;
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return columnSegments;
    }
}
