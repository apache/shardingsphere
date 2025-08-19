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
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Update SQL statement base context.
 */
@Getter
public final class UpdateStatementBaseContext implements SQLStatementContext {
    
    private final UpdateStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final Collection<WhereSegment> whereSegments;
    
    private final Collection<ColumnSegment> columnSegments;
    
    private final Collection<BinaryOperationExpression> joinConditions = new LinkedList<>();
    
    public UpdateStatementBaseContext(final UpdateStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(getAllSimpleTableSegments());
        whereSegments = createWhereSegments(sqlStatement);
        columnSegments = ColumnExtractor.extractColumnSegments(whereSegments);
        ExpressionExtractor.extractJoinConditions(joinConditions, whereSegments);
    }
    
    private Collection<WhereSegment> createWhereSegments(final UpdateStatement updateStatement) {
        Collection<WhereSegment> result = new LinkedList<>();
        updateStatement.getWhere().ifPresent(result::add);
        return result;
    }
    
    private Collection<SimpleTableSegment> getAllSimpleTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromUpdate(sqlStatement);
        return tableExtractor.getRewriteTables();
    }
}
