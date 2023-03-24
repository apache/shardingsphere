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
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBasePropertyKey;
import org.apache.shardingsphere.proxy.backend.hbase.util.HBaseHeterogeneousUtils;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import java.util.Optional;

/**
 * Checker for select statement.
 */
public class HeterogeneousSelectStatementChecker extends CommonHeterogeneousSQLStatementChecker<SelectStatement> {
    
    public HeterogeneousSelectStatementChecker(final SelectStatement sqlStatement) {
        super(sqlStatement);
    }
    
    @Override
    public void execute() {
        checkProjectionsIsExpected();
        checkDoNotSupportedSegment();
        checkSupportedWhereSegment();
        checkSupportedOrderBySegment();
    }
    
    private void checkDoNotSupportedSegment() {
        Preconditions.checkArgument(getSqlStatement().getFrom() instanceof SimpleTableSegment, "Only supported SimpleTableSegment");
        Preconditions.checkArgument(!getSqlStatement().getHaving().isPresent(), "Do not supported having segment");
        Preconditions.checkArgument(!getSqlStatement().getGroupBy().isPresent(), "Do not supported group by segment");
        
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) getSqlStatement();
        Preconditions.checkArgument(!selectStatement.getWindow().isPresent(), "Do not supported window segment");
        Preconditions.checkArgument(!selectStatement.getLock().isPresent(), "Do not supported lock segment");
        Optional<LimitSegment> limitSegment = selectStatement.getLimit();
        if (limitSegment.isPresent()) {
            Preconditions.checkArgument(!selectStatement.getLimit().get().getOffset().isPresent(), "Do not supported offset segment");
            Optional<PaginationValueSegment> paginationSegment = selectStatement.getLimit().flatMap(LimitSegment::getRowCount);
            Long maxScanLimitSize = HBaseContext.getInstance().getProps().<Long>getValue(HBasePropertyKey.MAX_SCAN_LIMIT_SIZE);
            paginationSegment.ifPresent(valueSegment -> Preconditions.checkArgument(((NumberLiteralLimitValueSegment) valueSegment).getValue() <= maxScanLimitSize, "row count must less than 5000"));
        }
    }
    
    private void checkProjectionsIsExpected() {
        for (ProjectionSegment projectionSegment : getSqlStatement().getProjections().getProjections()) {
            if (!(projectionSegment instanceof ShorthandProjectionSegment || projectionSegment instanceof ColumnProjectionSegment || HBaseHeterogeneousUtils.isCrcProjectionSegment(
                    projectionSegment))) {
                throw new IllegalArgumentException("Only supported ShorthandProjection and ColumnProjection and crc32ExpressionProjection");
            }
        }
    }
    
    private void checkSupportedWhereSegment() {
        Optional<WhereSegment> whereSegment = getSqlStatement().getWhere();
        if (!whereSegment.isPresent()) {
            return;
        }
        ExpressionSegment whereExpr = whereSegment.get().getExpr();
        if (whereExpr instanceof BinaryOperationExpression) {
            checkIsSinglePointQuery(whereSegment);
        } else if (whereExpr instanceof InExpression) {
            checkInExpressionIsExpected(whereExpr);
        } else if (whereExpr instanceof BetweenExpression) {
            checkBetweenExpressionIsExpected(whereExpr);
        } else {
            throw new IllegalArgumentException("Only supported =、in、between...and...");
        }
    }
    
    private void checkBetweenExpressionIsExpected(final ExpressionSegment whereExpr) {
        BetweenExpression expression = (BetweenExpression) whereExpr;
        
        Preconditions.checkArgument(expression.getLeft() instanceof ColumnSegment, "left segment must is ColumnSegment");
        String rowKey = ((ColumnSegment) expression.getLeft()).getIdentifier().getValue();
        boolean isAllowKey = ALLOW_KEYS.stream().anyMatch(each -> each.equalsIgnoreCase(rowKey));
        Preconditions.checkArgument(isAllowKey, String.format("%s is not a allowed key", rowKey));
        
        Preconditions.checkArgument(!expression.isNot(), "Do not supported `not between...and...`");
        Preconditions.checkArgument(isAllowExpressionSegment(expression.getBetweenExpr()), "between expr must is literal or parameter marker");
        Preconditions.checkArgument(isAllowExpressionSegment(expression.getAndExpr()), "between expr must is literal or parameter marker");
    }
    
    private void checkSupportedOrderBySegment() {
        if (!getSqlStatement().getOrderBy().isPresent()) {
            return;
        }
        for (OrderByItemSegment orderByItemSegment : getSqlStatement().getOrderBy().get().getOrderByItems()) {
            if (!(orderByItemSegment instanceof ColumnOrderByItemSegment)) {
                throw new IllegalArgumentException("Only simple rowKey order by");
            }
            if (!"rowKey".equalsIgnoreCase(((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getIdentifier().getValue())) {
                throw new IllegalArgumentException("Only simple rowKey order by");
            }
        }
    }
    
}
