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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingTokenUtils;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding order by token generator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class ShardingOrderByTokenGenerator implements OptionalSQLTokenGenerator<SelectStatementContext>, IgnoreForSingleRoute, RouteContextAware {
    
    private final ShardingRule rule;
    
    private RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getOrderByContext().isGenerated();
    }
    
    @Override
    public OrderByToken generateSQLToken(final SelectStatementContext selectStatementContext) {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(selectStatementContext.getSqlStatement());
        Map<RouteUnit, Collection<String>> orderByItems = new HashMap<>(routeContext.getRouteUnits().size(), 1F);
        for (RouteUnit each : routeContext.getRouteUnits()) {
            orderByItems.put(each, getOrderByItems(selectStatementContext, tableExtractor, ShardingTokenUtils.getLogicAndActualTableMap(each, selectStatementContext, rule)));
        }
        return new OrderByToken(getGenerateOrderByStartIndex(selectStatementContext), getOrderByItems(selectStatementContext, tableExtractor, Collections.emptyMap()), orderByItems);
    }
    
    private Collection<String> getOrderByItems(final SelectStatementContext selectStatementContext, final TableExtractor tableExtractor, final Map<String, String> logicAndActualTables) {
        Collection<String> result = new LinkedList<>();
        for (OrderByItem each : selectStatementContext.getOrderByContext().getItems()) {
            result.add(String.join(" ", getColumnLabel(each, tableExtractor, logicAndActualTables), each.getSegment().getOrderDirection().name()));
        }
        return result;
    }
    
    private String getColumnLabel(final OrderByItem orderByItem, final TableExtractor tableExtractor, final Map<String, String> logicAndActualTables) {
        if (orderByItem.getSegment() instanceof ColumnOrderByItemSegment) {
            return getColumnLabel((ColumnOrderByItemSegment) orderByItem.getSegment(), tableExtractor, logicAndActualTables);
        }
        if (orderByItem.getSegment() instanceof ExpressionOrderByItemSegment) {
            return ((ExpressionOrderByItemSegment) orderByItem.getSegment()).getText();
        }
        return String.valueOf(orderByItem.getIndex());
    }
    
    private String getColumnLabel(final ColumnOrderByItemSegment columnOrderByItemSegment, final TableExtractor tableExtractor, final Map<String, String> logicAndActualTables) {
        Optional<OwnerSegment> owner = columnOrderByItemSegment.getColumn().getOwner();
        if (!owner.isPresent() || !tableExtractor.needRewrite(owner.get()) || !logicAndActualTables.containsKey(owner.get().getIdentifier().getValue())) {
            return columnOrderByItemSegment.getText();
        }
        String actualOwner = owner.get().getIdentifier().getQuoteCharacter().wrap(logicAndActualTables.get(owner.get().getIdentifier().getValue()));
        return String.join(".", actualOwner, columnOrderByItemSegment.getColumn().getIdentifier().getValueWithQuoteCharacters());
    }
    
    private int getGenerateOrderByStartIndex(final SelectStatementContext selectStatementContext) {
        SelectStatement sqlStatement = selectStatementContext.getSqlStatement();
        int stopIndex;
        if (sqlStatement.getWindow().isPresent()) {
            stopIndex = sqlStatement.getWindow().get().getStopIndex();
        } else if (sqlStatement.getHaving().isPresent()) {
            stopIndex = sqlStatement.getHaving().get().getStopIndex();
        } else if (sqlStatement.getGroupBy().isPresent()) {
            stopIndex = sqlStatement.getGroupBy().get().getStopIndex();
        } else if (sqlStatement.getWhere().isPresent()) {
            stopIndex = sqlStatement.getWhere().get().getStopIndex();
        } else {
            stopIndex = selectStatementContext.getTablesContext().getSimpleTables().stream().mapToInt(SimpleTableSegment::getStopIndex).max().orElse(0);
        }
        return stopIndex + 1;
    }
}
