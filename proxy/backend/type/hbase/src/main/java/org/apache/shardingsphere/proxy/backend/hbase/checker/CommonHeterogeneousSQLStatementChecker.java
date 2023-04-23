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

package org.apache.shardingsphere.proxy.backend.hbase.checker;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Abstract checker.
 * 
 * @param <T> SQLStatement.
 */
@Getter
public class CommonHeterogeneousSQLStatementChecker<T extends SQLStatement> implements HeterogeneousSQLStatementChecker<T> {
    
    protected static final List<String> ALLOW_KEYS = Arrays.asList("rowKey", "row_key", "key", "pk", "id");
    
    private final T sqlStatement;
    
    public CommonHeterogeneousSQLStatementChecker(final T sqlStatement) {
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    public void execute() {
        
    }
    
    protected void checkIsSinglePointQuery(final Optional<WhereSegment> whereSegment) {
        Preconditions.checkArgument(whereSegment.isPresent(), "Must Have Where Segment");
        ExpressionSegment whereExpr = whereSegment.get().getExpr();
        Preconditions.checkArgument(whereExpr instanceof BinaryOperationExpression, "Only Support BinaryOperationExpression");
        BinaryOperationExpression expression = (BinaryOperationExpression) whereExpr;
        Preconditions.checkArgument(!(expression.getLeft() instanceof BinaryOperationExpression), "Do not supported Multiple expressions");
        Preconditions.checkArgument(expression.getLeft() instanceof ColumnSegment, "left segment must is ColumnSegment");
        Preconditions.checkArgument("=".equals(expression.getOperator()), "Only Supported `=` operator");
        String rowKey = ((ColumnSegment) expression.getLeft()).getIdentifier().getValue();
        boolean isAllowKey = ALLOW_KEYS.stream().anyMatch(each -> each.equalsIgnoreCase(rowKey));
        Preconditions.checkArgument(isAllowKey, String.format("%s is not a allowed key", rowKey));
    }
    
    /**
     * Check value is literal or parameter marker.
     * 
     * @param expressionSegment value segment
     * 
     * @return is supported
     */
    protected boolean isAllowExpressionSegment(final ExpressionSegment expressionSegment) {
        return expressionSegment instanceof LiteralExpressionSegment || expressionSegment instanceof ParameterMarkerExpressionSegment;
    }
    
    /**
     * Check in expression.
     *
     * @param whereExpr InExpression
     */
    protected void checkInExpressionIsExpected(final ExpressionSegment whereExpr) {
        InExpression expression = (InExpression) whereExpr;
        Preconditions.checkArgument(expression.getLeft() instanceof ColumnSegment, "left segment must is ColumnSegment");
        String rowKey = ((ColumnSegment) expression.getLeft()).getIdentifier().getValue();
        boolean isAllowKey = ALLOW_KEYS.stream().anyMatch(each -> each.equalsIgnoreCase(rowKey));
        Preconditions.checkArgument(isAllowKey, String.format("%s is not a allowed key", rowKey));
        Preconditions.checkArgument(!expression.isNot(), "Do not supported `not in`");
        Preconditions.checkArgument(expression.getRight() instanceof ListExpression, "Only supported ListExpression");
    }
}
