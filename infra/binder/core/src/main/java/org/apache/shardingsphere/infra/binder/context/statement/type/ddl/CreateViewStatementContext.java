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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Create view statement context.
 */
@Getter
public final class CreateViewStatementContext implements SQLStatementContext, WhereContextAvailable {
    
    private final CreateViewStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final SelectStatementContext selectStatementContext;
    
    public CreateViewStatementContext(final ShardingSphereMetaData metaData, final CreateViewStatement sqlStatement, final String currentDatabaseName) {
        this.sqlStatement = sqlStatement;
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromCreateViewStatement(sqlStatement);
        tablesContext = new TablesContext(extractor.getRewriteTables());
        selectStatementContext = new SelectStatementContext(sqlStatement.getSelect(), metaData, currentDatabaseName, Collections.emptyList());
        selectStatementContext.setSubqueryType(SubqueryType.VIEW_DEFINITION);
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return selectStatementContext.getWhereSegments();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return selectStatementContext.getColumnSegments();
    }
    
    @Override
    public Collection<BinaryOperationExpression> getJoinConditions() {
        return selectStatementContext.getJoinConditions();
    }
}
