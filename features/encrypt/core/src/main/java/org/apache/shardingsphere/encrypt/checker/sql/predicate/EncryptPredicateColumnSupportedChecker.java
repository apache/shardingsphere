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

import org.apache.shardingsphere.encrypt.exception.metadata.MissingMatchedEncryptQueryAlgorithmException;
import org.apache.shardingsphere.encrypt.rewrite.token.comparator.JoinConditionsEncryptorComparator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Predicate column supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptPredicateColumnSupportedChecker implements SupportedSQLChecker<SQLStatementContext, EncryptRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatementContext> allSubqueryContexts = SQLStatementContextExtractor.getAllSubqueryContexts(sqlStatementContext);
        Collection<BinaryOperationExpression> joinConditions = SQLStatementContextExtractor.getJoinConditions((WhereAvailable) sqlStatementContext, allSubqueryContexts);
        ShardingSpherePreconditions.checkState(JoinConditionsEncryptorComparator.isSame(joinConditions, rule),
                () -> new UnsupportedSQLOperationException("Can not use different encryptor in join condition"));
        check(rule, currentSchema, (WhereAvailable) sqlStatementContext);
    }
    
    private void check(final EncryptRule rule, final ShardingSphereSchema schema, final WhereAvailable sqlStatementContext) {
        Map<String, String> columnExpressionTableNames = ((TableAvailable) sqlStatementContext).getTablesContext().findTableNames(sqlStatementContext.getColumnSegments(), schema);
        for (ColumnSegment each : sqlStatementContext.getColumnSegments()) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(columnExpressionTableNames.getOrDefault(each.getExpression(), ""));
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
            Collection<AndPredicate> andPredicates = ExpressionExtractor.extractAndPredicates(each.getExpr());
            for (AndPredicate andPredicate : andPredicates) {
                if (isLikeColumnSegment(andPredicate, targetColumnSegment)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isLikeColumnSegment(final AndPredicate andPredicate, final ColumnSegment targetColumnSegment) {
        for (ExpressionSegment each : andPredicate.getPredicates()) {
            if (each instanceof BinaryOperationExpression
                    && "LIKE".equalsIgnoreCase(((BinaryOperationExpression) each).getOperator()) && isSameColumnSegment(((BinaryOperationExpression) each).getLeft(), targetColumnSegment)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameColumnSegment(final ExpressionSegment columnSegment, final ColumnSegment targetColumnSegment) {
        return columnSegment instanceof ColumnSegment && columnSegment.getStartIndex() == targetColumnSegment.getStartIndex() && columnSegment.getStopIndex() == targetColumnSegment.getStopIndex();
    }
}
