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

package org.apache.shardingsphere.encrypt.checker.sql.predicate;

import org.apache.shardingsphere.encrypt.checker.cryptographic.JoinConditionsEncryptorChecker;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingMatchedEncryptQueryAlgorithmException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.Optional;

/**
 * Predicate column supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptPredicateColumnSupportedChecker implements SupportedSQLChecker<SQLStatementContext, EncryptRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereContextAvailable && !((WhereContextAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatementContext> allSubqueryContexts = SQLStatementContextExtractor.getAllSubqueryContexts(sqlStatementContext);
        Collection<BinaryOperationExpression> joinConditions = SQLStatementContextExtractor.getJoinConditions((WhereContextAvailable) sqlStatementContext, allSubqueryContexts);
        JoinConditionsEncryptorChecker.checkIsSame(joinConditions, rule, "join condition");
        check(rule, (WhereContextAvailable) sqlStatementContext);
    }
    
    private void check(final EncryptRule rule, final WhereContextAvailable sqlStatementContext) {
        for (ColumnSegment each : sqlStatementContext.getColumnSegments()) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(each.getColumnBoundInfo().getOriginalTable().getValue());
            String columnName = each.getIdentifier().getValue();
            if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnName) && includesLike(sqlStatementContext.getWhereSegments(), each)) {
                String tableName = encryptTable.get().getTable();
                ShardingSpherePreconditions.checkState(
                        encryptTable.get().getEncryptColumn(columnName).getLikeQuery().isPresent(), () -> new MissingMatchedEncryptQueryAlgorithmException(tableName, columnName, "LIKE"));
            }
        }
    }
    
    private boolean includesLike(final Collection<WhereSegment> whereSegments, final ColumnSegment targetColumnSegment) {
        for (WhereSegment each : whereSegments) {
            Collection<ExpressionSegment> expressions = ExpressionExtractor.extractAllExpressions(each.getExpr());
            if (isLikeColumnSegment(expressions, targetColumnSegment)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isLikeColumnSegment(final Collection<ExpressionSegment> expressions, final ColumnSegment targetColumnSegment) {
        for (ExpressionSegment each : expressions) {
            if (each instanceof BinaryOperationExpression
                    && ("LIKE".equalsIgnoreCase(((BinaryOperationExpression) each).getOperator()) || "NOT LIKE".equalsIgnoreCase(((BinaryOperationExpression) each).getOperator()))
                    && isSameColumnSegment(((BinaryOperationExpression) each).getLeft(), targetColumnSegment)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameColumnSegment(final ExpressionSegment columnSegment, final ColumnSegment targetColumnSegment) {
        return columnSegment instanceof ColumnSegment && columnSegment.getStartIndex() == targetColumnSegment.getStartIndex() && columnSegment.getStopIndex() == targetColumnSegment.getStopIndex();
    }
}
