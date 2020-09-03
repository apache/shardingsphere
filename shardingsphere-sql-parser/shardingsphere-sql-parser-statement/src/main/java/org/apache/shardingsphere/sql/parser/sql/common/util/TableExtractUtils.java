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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

public final class TableExtractUtils {
    
    @Getter
    private Collection<SimpleTableSegment> rewriteTables = new LinkedList<>();
    
    @Getter
    private Collection<TableSegment> tableContext = new LinkedList<>();
    
    /**
     * Extract table that should be rewrited from SelectStatement.
     *
     * @param selectStatement SelectStatement.
     */
    public void extractTablesFromSelect(final SelectStatement selectStatement) {
        if (null != selectStatement.getFrom()) {
            extractTablesFromTableSegment(selectStatement.getFrom());
        }
        if (selectStatement.getWhere().isPresent()) {
            extractTablesFromExpression(selectStatement.getWhere().get().getExpr());
        }
        if (null != selectStatement.getProjections()) {
            extractTablesFromProjections(selectStatement.getProjections());
        }
        if (selectStatement.getGroupBy().isPresent()) {
            extractTablesFromOrderByItems(selectStatement.getGroupBy().get().getGroupByItems());
        }
        if (selectStatement.getOrderBy().isPresent()) {
            extractTablesFromOrderByItems(selectStatement.getOrderBy().get().getOrderByItems());
        }
    }
    
    private void extractTablesFromTableSegment(final TableSegment tableSegment) {
        if (tableSegment instanceof SimpleTableSegment) {
            tableContext.add(tableSegment);
            rewriteTables.add((SimpleTableSegment) tableSegment);
        }
        if (tableSegment instanceof SubqueryTableSegment) {
            tableContext.add(tableSegment);
            TableExtractUtils utils = new TableExtractUtils();
            utils.extractTablesFromSelect(((SubqueryTableSegment) tableSegment).getSubquery().getSelect());
            rewriteTables.addAll(utils.getRewriteTables());
        }
        if (tableSegment instanceof JoinTableSegment) {
            extractTablesFromTableSegment(((JoinTableSegment) tableSegment).getLeft());
            extractTablesFromTableSegment(((JoinTableSegment) tableSegment).getRight());
            extractTablesFromExpression(((JoinTableSegment) tableSegment).getCondition());
        }
    }
    
    private void extractTablesFromExpression(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ColumnSegment) {
            if (((ColumnSegment) expressionSegment).getOwner().isPresent() && needRewrite(((ColumnSegment) expressionSegment).getOwner().get())) {
                OwnerSegment ownerSegment = ((ColumnSegment) expressionSegment).getOwner().get();
                rewriteTables.add(new SimpleTableSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
            }
        }
        if (expressionSegment instanceof ListExpression) {
            for (ExpressionSegment each : ((ListExpression) expressionSegment).getItems()) {
                extractTablesFromExpression(each);
            }
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getLeft());
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getRight());
        }
    }
    
    private void extractTablesFromProjections(final ProjectionsSegment projections) {
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                extractTablesFromSelect(((SubqueryProjectionSegment) each).getSubquery().getSelect());
            } else if (each instanceof OwnerAvailable) {
                if (((OwnerAvailable) each).getOwner().isPresent() && needRewrite(((OwnerAvailable) each).getOwner().get())) {
                    OwnerSegment ownerSegment = ((OwnerAvailable) each).getOwner().get();
                    rewriteTables.add(new SimpleTableSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
                }
            }
        }
    }
    
    private void extractTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && needRewrite(owner.get())) {
                    OwnerSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                    rewriteTables.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
                }
            }
        }
    }
    
    /**
     * Extract table that should be rewrited from DeleteStatement.
     *
     * @param deleteStatement DeleteStatement.
     */
    public void extractTablesFromDelete(final DeleteStatement deleteStatement) {
        
        extractTablesFromTableSegment(deleteStatement.getTableSegment());
        extractTablesFromExpression(deleteStatement.getWhere().get().getExpr());
    }
    
    /**
     * Extract table that should be rewrited from UpdateStatement.
     *
     * @param updateStatement UpdateStatement.
     */
    public void extractTablesFromUpdate(final UpdateStatement updateStatement) {
        
        extractTablesFromTableSegment(updateStatement.getTableSegment());
        extractTablesFromExpression(updateStatement.getWhere().get().getExpr());
    }
    
    private boolean needRewrite(final OwnerSegment owner) {
        for (TableSegment each : tableContext) {
            if (owner.getIdentifier().getValue().equals(each.getAlias().orElse(null))) {
                return false;
            }
        }
        return true;
    }
}
