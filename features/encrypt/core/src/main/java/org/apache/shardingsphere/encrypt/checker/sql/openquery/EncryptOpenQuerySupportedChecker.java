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

package org.apache.shardingsphere.encrypt.checker.sql.openquery;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Optional;

/**
 * OPENQUERY supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptOpenQuerySupportedChecker implements SupportedSQLChecker<SQLStatementContext, EncryptRule> {
    
    private static final String OPENQUERY_FUNCTION_NAME = "OPENQUERY";
    
    private static final String SQLSERVER_DATABASE_TYPE = "SQLServer";
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (!SQLSERVER_DATABASE_TYPE.equals(sqlStatement.getDatabaseType().getType())) {
            return false;
        }
        if (sqlStatement instanceof SelectStatement) {
            return containsOpenQueryInSelect((SelectStatement) sqlStatement);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return containsOpenQueryInUpdate((UpdateStatement) sqlStatement);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return containsOpenQueryInDelete((DeleteStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return containsOpenQueryInInsert((InsertStatement) sqlStatement);
        }
        if (sqlStatement instanceof MergeStatement) {
            return containsOpenQueryInMerge((MergeStatement) sqlStatement);
        }
        return false;
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        ShardingSpherePreconditions.checkState(false, () -> new UnsupportedEncryptSQLException("OPENQUERY"));
    }
    
    private boolean containsOpenQueryInSelect(final SelectStatement selectStatement) {
        return selectStatement.getFrom().map(this::containsOpenQuery).orElse(false)
                || containsOpenQueryInWhere(selectStatement.getWhere()) || containsOpenQueryInWith(selectStatement.getWith())
                || containsOpenQueryInSelectClauses(selectStatement);
    }
    
    private boolean containsOpenQueryInSelectClauses(final SelectStatement selectStatement) {
        return containsOpenQueryInCombine(selectStatement.getCombine())
                || containsOpenQueryInProjections(selectStatement.getProjections()) || containsOpenQueryInHaving(selectStatement.getHaving());
    }
    
    private boolean containsOpenQueryInUpdate(final UpdateStatement updateStatement) {
        return containsOpenQuery(updateStatement.getTable())
                || updateStatement.getFrom().map(this::containsOpenQuery).orElse(false)
                || containsOpenQueryInUpdateClauses(updateStatement);
    }
    
    private boolean containsOpenQueryInUpdateClauses(final UpdateStatement updateStatement) {
        return containsOpenQueryInWhere(updateStatement.getWhere()) || containsOpenQueryInWith(updateStatement.getWith())
                || containsOpenQueryInAssignments(updateStatement);
    }
    
    private boolean containsOpenQueryInDelete(final DeleteStatement deleteStatement) {
        return containsOpenQuery(deleteStatement.getTable())
                || containsOpenQueryInWhere(deleteStatement.getWhere()) || containsOpenQueryInWith(deleteStatement.getWith());
    }
    
    private boolean containsOpenQueryInInsert(final InsertStatement insertStatement) {
        if (insertStatement.getRowSetFunction().map(this::isOpenQueryFunction).orElse(false)) {
            return true;
        }
        return insertStatement.getInsertSelect().map(each -> containsOpenQueryInSelect(each.getSelect())).orElse(false)
                || containsOpenQueryInWith(insertStatement.getWith());
    }
    
    private boolean containsOpenQueryInMerge(final MergeStatement mergeStatement) {
        return containsOpenQuery(mergeStatement.getTarget()) || containsOpenQuery(mergeStatement.getSource())
                || containsOpenQueryInWith(mergeStatement.getWith()) || containsOpenQueryInWhenAndThens(mergeStatement);
    }
    
    private boolean containsOpenQueryInWhenAndThens(final MergeStatement mergeStatement) {
        for (MergeWhenAndThenSegment whenAndThen : mergeStatement.getWhenAndThens()) {
            if (null != whenAndThen.getAndExpr() && containsOpenQueryInExpression(whenAndThen.getAndExpr())) {
                return true;
            }
            if (null != whenAndThen.getUpdate() && containsOpenQueryInUpdate(whenAndThen.getUpdate())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsOpenQueryInWhere(final Optional<WhereSegment> whereSegment) {
        return whereSegment.map(each -> containsOpenQueryInExpression(each.getExpr())).orElse(false);
    }
    
    private boolean containsOpenQueryInWith(final Optional<WithSegment> withSegment) {
        return withSegment.map(each -> each.getCommonTableExpressions().stream().anyMatch(this::containsOpenQueryInCTE)).orElse(false);
    }
    
    private boolean containsOpenQueryInCTE(final CommonTableExpressionSegment cte) {
        return containsOpenQueryInSelect(cte.getSubquery().getSelect());
    }
    
    private boolean containsOpenQueryInCombine(final Optional<CombineSegment> combineSegment) {
        return combineSegment.map(each -> containsOpenQueryInSelect(each.getLeft().getSelect()) || containsOpenQueryInSelect(each.getRight().getSelect())).orElse(false);
    }
    
    private boolean containsOpenQueryInProjections(final ProjectionsSegment projectionsSegment) {
        for (ProjectionSegment projection : projectionsSegment.getProjections()) {
            if (projection instanceof SubqueryProjectionSegment && containsOpenQueryInSelect(((SubqueryProjectionSegment) projection).getSubquery().getSelect())) {
                return true;
            }
            if (projection instanceof ExpressionProjectionSegment && containsOpenQueryInExpression(((ExpressionProjectionSegment) projection).getExpr())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsOpenQueryInAssignments(final UpdateStatement updateStatement) {
        return updateStatement.getAssignment().map(each -> each.getAssignments().stream().anyMatch(assign -> containsOpenQueryInExpression(assign.getValue()))).orElse(false);
    }
    
    private boolean containsOpenQueryInHaving(final Optional<HavingSegment> havingSegment) {
        return havingSegment.map(each -> containsOpenQueryInExpression(each.getExpr())).orElse(false);
    }
    
    private boolean containsOpenQueryInExpression(final ExpressionSegment expression) {
        if (expression instanceof ExistsSubqueryExpression) {
            return containsOpenQueryInSelect(((ExistsSubqueryExpression) expression).getSubquery().getSelect());
        }
        if (expression instanceof SubqueryExpressionSegment) {
            return containsOpenQueryInSelect(((SubqueryExpressionSegment) expression).getSubquery().getSelect());
        }
        if (expression instanceof QuantifySubqueryExpression) {
            return containsOpenQueryInSelect(((QuantifySubqueryExpression) expression).getSubquery().getSelect());
        }
        if (expression instanceof BinaryOperationExpression) {
            return containsOpenQueryInExpression(((BinaryOperationExpression) expression).getLeft())
                    || containsOpenQueryInExpression(((BinaryOperationExpression) expression).getRight());
        }
        if (expression instanceof InExpression) {
            return containsOpenQueryInExpression(((InExpression) expression).getLeft())
                    || containsOpenQueryInExpression(((InExpression) expression).getRight());
        }
        return false;
    }
    
    private boolean containsOpenQuery(final TableSegment tableSegment) {
        if (tableSegment instanceof FunctionTableSegment) {
            return isOpenQuery((FunctionTableSegment) tableSegment);
        }
        if (tableSegment instanceof JoinTableSegment) {
            return containsOpenQuery(((JoinTableSegment) tableSegment).getLeft()) || containsOpenQuery(((JoinTableSegment) tableSegment).getRight());
        }
        if (tableSegment instanceof SubqueryTableSegment) {
            return containsOpenQueryInSelect(((SubqueryTableSegment) tableSegment).getSubquery().getSelect());
        }
        if (tableSegment instanceof DeleteMultiTableSegment) {
            return containsOpenQuery(((DeleteMultiTableSegment) tableSegment).getRelationTable());
        }
        return false;
    }
    
    private boolean isOpenQuery(final FunctionTableSegment functionTableSegment) {
        ExpressionSegment tableFunction = functionTableSegment.getTableFunction();
        return tableFunction instanceof FunctionSegment && isOpenQueryFunction((FunctionSegment) tableFunction);
    }
    
    private boolean isOpenQueryFunction(final FunctionSegment functionSegment) {
        return OPENQUERY_FUNCTION_NAME.equalsIgnoreCase(functionSegment.getFunctionName());
    }
}
