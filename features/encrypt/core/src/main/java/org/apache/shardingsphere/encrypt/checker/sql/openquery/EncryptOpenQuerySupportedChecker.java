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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
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

import java.util.Collection;

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
                || selectStatement.getWhere().map(optional -> containsOpenQueryInExpression(optional.getExpr())).orElse(false)
                || selectStatement.getWith().map(this::containsOpenQueryInWith).orElse(false) || containsOpenQueryInSelectClauses(selectStatement);
    }
    
    private boolean containsOpenQueryInSelectClauses(final SelectStatement selectStatement) {
        return selectStatement.getCombine().map(optional -> containsOpenQueryInSelect(optional.getLeft().getSelect()) || containsOpenQueryInSelect(optional.getRight().getSelect())).orElse(false)
                || containsOpenQueryInProjections(selectStatement.getProjections())
                || selectStatement.getHaving().map(optional -> containsOpenQueryInExpression(optional.getExpr())).orElse(false);
    }
    
    private boolean containsOpenQueryInUpdate(final UpdateStatement updateStatement) {
        return containsOpenQuery(updateStatement.getTable())
                || updateStatement.getFrom().map(this::containsOpenQuery).orElse(false)
                || containsOpenQueryInUpdateClauses(updateStatement);
    }
    
    private boolean containsOpenQueryInUpdateClauses(final UpdateStatement updateStatement) {
        return updateStatement.getWhere().map(optional -> containsOpenQueryInExpression(optional.getExpr())).orElse(false)
                || updateStatement.getWith().map(this::containsOpenQueryInWith).orElse(false) || containsOpenQueryInAssignments(updateStatement);
    }
    
    private boolean containsOpenQueryInDelete(final DeleteStatement deleteStatement) {
        return containsOpenQuery(deleteStatement.getTable())
                || deleteStatement.getWhere().map(optional -> containsOpenQueryInExpression(optional.getExpr())).orElse(false)
                || deleteStatement.getWith().map(this::containsOpenQueryInWith).orElse(false);
    }
    
    private boolean containsOpenQueryInInsert(final InsertStatement insertStatement) {
        if (insertStatement.getRowSetFunction().map(this::isOpenQueryFunction).orElse(false)) {
            return true;
        }
        return insertStatement.getInsertSelect().map(optional -> containsOpenQueryInSelect(optional.getSelect())).orElse(false)
                || insertStatement.getWith().map(this::containsOpenQueryInWith).orElse(false);
    }
    
    private boolean containsOpenQueryInMerge(final MergeStatement mergeStatement) {
        return containsOpenQuery(mergeStatement.getTarget()) || containsOpenQuery(mergeStatement.getSource())
                || containsOpenQueryInMergeClauses(mergeStatement);
    }
    
    private boolean containsOpenQueryInMergeClauses(final MergeStatement mergeStatement) {
        if (mergeStatement.getWith().map(this::containsOpenQueryInWith).orElse(false)) {
            return true;
        }
        if (null != mergeStatement.getExpression() && containsOpenQueryInExpression(mergeStatement.getExpression().getExpr())) {
            return true;
        }
        return containsOpenQueryInWhenAndThens(mergeStatement);
    }
    
    private boolean containsOpenQueryInWhenAndThens(final MergeStatement mergeStatement) {
        for (MergeWhenAndThenSegment each : mergeStatement.getWhenAndThens()) {
            if (null != each.getAndExpr() && containsOpenQueryInExpression(each.getAndExpr())) {
                return true;
            }
            if (null != each.getUpdate() && containsOpenQueryInUpdate(each.getUpdate())) {
                return true;
            }
            if (null != each.getInsert() && containsOpenQueryInInsert(each.getInsert())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsOpenQueryInWith(final WithSegment withSegment) {
        for (CommonTableExpressionSegment each : withSegment.getCommonTableExpressions()) {
            if (containsOpenQueryInSelect(each.getSubquery().getSelect())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsOpenQueryInProjections(final ProjectionsSegment projectionsSegment) {
        for (ProjectionSegment each : projectionsSegment.getProjections()) {
            if (each instanceof SubqueryProjectionSegment && containsOpenQueryInSelect(((SubqueryProjectionSegment) each).getSubquery().getSelect())) {
                return true;
            }
            if (each instanceof ExpressionProjectionSegment && containsOpenQueryInExpression(((ExpressionProjectionSegment) each).getExpr())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsOpenQueryInAssignments(final UpdateStatement updateStatement) {
        return updateStatement.getAssignment().map(optional -> containsOpenQueryInColumnAssignments(optional.getAssignments())).orElse(false);
    }
    
    private boolean containsOpenQueryInColumnAssignments(final Collection<ColumnAssignmentSegment> assignments) {
        for (ColumnAssignmentSegment each : assignments) {
            if (containsOpenQueryInExpression(each.getValue())) {
                return true;
            }
        }
        return false;
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
        return containsOpenQueryInCompoundExpression(expression);
    }
    
    private boolean containsOpenQueryInCompoundExpression(final ExpressionSegment expression) {
        if (expression instanceof BinaryOperationExpression) {
            return containsOpenQueryInExpression(((BinaryOperationExpression) expression).getLeft())
                    || containsOpenQueryInExpression(((BinaryOperationExpression) expression).getRight());
        }
        if (expression instanceof InExpression) {
            return containsOpenQueryInExpression(((InExpression) expression).getLeft())
                    || containsOpenQueryInExpression(((InExpression) expression).getRight());
        }
        if (expression instanceof NotExpression) {
            return containsOpenQueryInExpression(((NotExpression) expression).getExpression());
        }
        if (expression instanceof BetweenExpression) {
            return containsOpenQueryInExpression(((BetweenExpression) expression).getLeft())
                    || containsOpenQueryInExpression(((BetweenExpression) expression).getBetweenExpr())
                    || containsOpenQueryInExpression(((BetweenExpression) expression).getAndExpr());
        }
        if (expression instanceof CaseWhenExpression) {
            return containsOpenQueryInCaseWhen((CaseWhenExpression) expression);
        }
        if (expression instanceof FunctionSegment) {
            return containsOpenQueryInFunctionParams((FunctionSegment) expression);
        }
        return false;
    }
    
    private boolean containsOpenQueryInFunctionParams(final FunctionSegment functionSegment) {
        for (ExpressionSegment each : functionSegment.getParameters()) {
            if (containsOpenQueryInExpression(each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsOpenQueryInCaseWhen(final CaseWhenExpression caseWhen) {
        if (null != caseWhen.getCaseExpr() && containsOpenQueryInExpression(caseWhen.getCaseExpr())) {
            return true;
        }
        for (ExpressionSegment each : caseWhen.getWhenExprs()) {
            if (containsOpenQueryInExpression(each)) {
                return true;
            }
        }
        for (ExpressionSegment each : caseWhen.getThenExprs()) {
            if (containsOpenQueryInExpression(each)) {
                return true;
            }
        }
        return null != caseWhen.getElseExpr() && containsOpenQueryInExpression(caseWhen.getElseExpr());
    }
    
    private boolean containsOpenQuery(final TableSegment tableSegment) {
        if (tableSegment instanceof FunctionTableSegment) {
            return isOpenQuery((FunctionTableSegment) tableSegment);
        }
        if (tableSegment instanceof JoinTableSegment) {
            return containsOpenQueryInJoin((JoinTableSegment) tableSegment);
        }
        if (tableSegment instanceof SubqueryTableSegment) {
            return containsOpenQueryInSelect(((SubqueryTableSegment) tableSegment).getSubquery().getSelect());
        }
        if (tableSegment instanceof DeleteMultiTableSegment) {
            return containsOpenQuery(((DeleteMultiTableSegment) tableSegment).getRelationTable());
        }
        return false;
    }
    
    private boolean containsOpenQueryInJoin(final JoinTableSegment joinTableSegment) {
        if (containsOpenQuery(joinTableSegment.getLeft()) || containsOpenQuery(joinTableSegment.getRight())) {
            return true;
        }
        return null != joinTableSegment.getCondition() && containsOpenQueryInExpression(joinTableSegment.getCondition());
    }
    
    private boolean isOpenQuery(final FunctionTableSegment functionTableSegment) {
        ExpressionSegment tableFunction = functionTableSegment.getTableFunction();
        return tableFunction instanceof FunctionSegment && isOpenQueryFunction((FunctionSegment) tableFunction);
    }
    
    private boolean isOpenQueryFunction(final FunctionSegment functionSegment) {
        return OPENQUERY_FUNCTION_NAME.equalsIgnoreCase(functionSegment.getFunctionName());
    }
}
