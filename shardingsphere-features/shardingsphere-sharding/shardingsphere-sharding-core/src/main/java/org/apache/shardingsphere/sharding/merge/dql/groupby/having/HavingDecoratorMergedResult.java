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

package org.apache.shardingsphere.sharding.merge.dql.groupby.having;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.shardingsphere.infra.binder.segment.select.having.HavingColumn;
import org.apache.shardingsphere.infra.binder.segment.select.having.HavingContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Decorator merged result for having.
 */
public final class HavingDecoratorMergedResult extends MemoryMergedResult<ShardingRule> {
    
    private static final GroovyShell SHELL = new GroovyShell();
    
    public HavingDecoratorMergedResult(final SQLStatementContext sqlStatementContext, final MergedResult mergedResult) throws SQLException {
        super(sqlStatementContext, mergedResult);
    }
    
    @Override
    protected List<MemoryQueryResultRow> init(final ShardingRule rule, final ShardingSphereSchema schema, final SQLStatementContext sqlStatementContext, 
                                              final List<QueryResult> queryResults, final MergedResult mergedResult) throws SQLException {
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        HavingContext havingContext = selectStatementContext.getHavingContext();
        ProjectionsContext projectionsContext = selectStatementContext.getProjectionsContext();
        List<MemoryQueryResultRow> result = new LinkedList<>();
        while (mergedResult.next()) {
            MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(mergedResult, projectionsContext.getProjections().size());
            if (havingContext.isHasHaving()) {
                // TODO support more expr scenario, like in, between and ... 
                Object evaluate = evaluate(generateHavingExpression(havingContext, memoryResultSetRow));
                if (evaluate instanceof Boolean && ((Boolean) evaluate)) {
                    result.add(memoryResultSetRow);
                }
            } else {
                result.add(memoryResultSetRow);
            }
        }
        return result;
    }
    
    private String generateHavingExpression(final HavingContext havingContext, final MemoryQueryResultRow memoryResultSetRow) {
        String expression = havingContext.getHavingExpression().toLowerCase();
        expression = expression.replaceAll("\\s+=\\s+", " == ").replaceAll("\\s+and\\s+", " && ").replaceAll("\\s+or\\s+", " || ");
        for (HavingColumn each : havingContext.getColumns()) {
            expression = expression.replace(each.getSegment().getIdentifier().getValue().toLowerCase(), memoryResultSetRow.getCell(each.getIndex()).toString());
        }
        return expression;
    }
    
    private Object evaluate(final String expression) {
        Script script = SHELL.parse(expression);
        return script.run();
    }
}
