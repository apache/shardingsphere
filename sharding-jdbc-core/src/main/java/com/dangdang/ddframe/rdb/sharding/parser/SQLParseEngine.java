/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser;

import com.alibaba.druid.sql.context.AggregationSelectItemContext;
import com.alibaba.druid.sql.context.GroupByContext;
import com.alibaba.druid.sql.context.InsertSQLContext;
import com.alibaba.druid.sql.context.ItemsToken;
import com.alibaba.druid.sql.context.OrderByContext;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.SelectItemContext;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 不包含OR语句的SQL构建器解析.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class SQLParseEngine {
    
    private final SQLContext sqlContext;
    
    /**
     *  解析SQL.
     * 
     * @return SQL解析结果
     */
    public SQLParsedResult parse() {
        ParseContext parseContext = getParseContext(sqlContext);
        if (sqlContext instanceof SelectSQLContext) {
            parseSelect(parseContext, (SelectSQLContext) sqlContext);
        } else if (sqlContext instanceof InsertSQLContext) {
            parseInsert(parseContext, (InsertSQLContext) sqlContext);
        }
        parseContext.getParsedResult().getRouteContext().setSqlBuilder(sqlContext.toSqlBuilder());
        return parseContext.getParsedResult();
    }
    
    private ParseContext getParseContext(final SQLContext sqlContext) {
        ParseContext result = new ParseContext(0);
        SQLParsedResult sqlParsedResult = result.getParsedResult();
        if (sqlContext.getConditionContexts().isEmpty()) {
            sqlParsedResult.getConditionContexts().add(new ConditionContext());
        } else {
            sqlParsedResult.getConditionContexts().addAll(sqlContext.getConditionContexts());
        }
        for (TableContext each : sqlContext.getTables()) {
            sqlParsedResult.getRouteContext().getTables().add(new Table(each.getName(), each.getAlias()));
        }
        sqlParsedResult.getRouteContext().setSqlStatementType(sqlContext.getType());
        return result;
    }
    
    private void parseInsert(final ParseContext parseContext, final InsertSQLContext sqlContext) {
        parseContext.getParsedResult().setGeneratedKeyContext(sqlContext.getGeneratedKeyContext());
    }
    
    private void parseSelect(final ParseContext parseContext, final SelectSQLContext sqlContext) {
        SQLParsedResult sqlParsedResult = parseContext.getParsedResult();
        for (SelectItemContext each : sqlContext.getItemContexts()) {
            parseContext.getSelectItems().add(each);
        }
        if (sqlContext.isContainStar()) {
            parseContext.registerSelectItem("*");
        }
        ItemsToken itemsToken = new ItemsToken(sqlContext.getSelectListLastPosition());
        
        
        for (SelectItemContext each : sqlContext.getItemContexts()) {
            if (each instanceof AggregationSelectItemContext) {
                AggregationSelectItemContext aggregationSelectItemContext = (AggregationSelectItemContext) each;
                // TODO index获取不准，考虑使用别名替换
                AggregationColumn column = new AggregationColumn(aggregationSelectItemContext.getExpression(), aggregationSelectItemContext.getAggregationType(),
                        aggregationSelectItemContext.getAlias(), Optional.<String>absent(), aggregationSelectItemContext.getIndex());
                sqlParsedResult.getMergeContext().getAggregationColumns().add(column);
                if (AggregationColumn.AggregationType.AVG.equals(aggregationSelectItemContext.getAggregationType())) {
                    List<AggregationColumn> aggregationColumns = parseContext.addDerivedColumnsForAvgColumn(column);
                    // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
                    for (AggregationColumn aggregationColumn : aggregationColumns) {
                        itemsToken.getItems().add(aggregationColumn.getExpression() + " AS " + aggregationColumn.getAlias().get() + " ");
                    }
                }
            }
        }
        
        if (!sqlContext.getGroupByContexts().isEmpty()) {
            for (GroupByContext each : sqlContext.getGroupByContexts()) {
                GroupByColumn groupByColumn = parseContext.addGroupByColumns(each.getOwner(), each.getName(), each.getOrderByType());
                boolean found = false;
                String groupByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName() :each.getName();
                for (SelectItemContext context : sqlContext.getItemContexts()) {
                    if ((!context.getAlias().isPresent() && context.getExpression().equalsIgnoreCase(groupByExpression))
                            || (context.getAlias().isPresent() && context.getAlias().get().equalsIgnoreCase(groupByExpression))) {
                        found = true;
                        break;
                    }
                }
                // TODO 需重构,目前的做法是通过补列有别名则补列,如果不包含select item则生成别名,进而补列,这里逻辑不直观
                if (!found && groupByColumn.getAlias().isPresent()) {
                    itemsToken.getItems().add(groupByExpression + " AS " + groupByColumn.getAlias().get() + " ");
                }
            }
        }
        
        if (!sqlContext.getOrderByContexts().isEmpty()) {
            for (OrderByContext each : sqlContext.getOrderByContexts()) {
                if (each.getIndex().isPresent()) {
                    parseContext.addOrderByColumn(each.getIndex().get(), each.getOrderByType());
                } else {
                    OrderByColumn orderByColumn = parseContext.addOrderByColumn(each.getOwner(), each.getName().get(), each.getOrderByType());
                    boolean found = false;
                    String orderByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName().get() : each.getName().get();
                    for (SelectItemContext context : sqlContext.getItemContexts()) {
                        if (context.getExpression().equalsIgnoreCase(orderByExpression) || orderByExpression.equalsIgnoreCase(context.getAlias().orNull())) {
                            found = true;
                            break;
                        }
                    }
                    // TODO 需重构,目前的做法是通过补列有别名则补列,如果不包含select item则生成别名,进而补列,这里逻辑不直观
                    if (!found && orderByColumn.getAlias().isPresent()) {
                        itemsToken.getItems().add(orderByExpression + " AS " + orderByColumn.getAlias().get() + " ");
                    }
                }
            }
        }
        
        if (!itemsToken.getItems().isEmpty()) {
            sqlContext.getSqlTokens().add(itemsToken);
        }
        
        
        if (null != sqlContext.getLimitContext()) {
            parseContext.getParsedResult().getMergeContext().setLimit(
                    new Limit(sqlContext.getLimitContext().getOffset().isPresent() ? sqlContext.getLimitContext().getOffset().get() : 0, sqlContext.getLimitContext().getRowCount(), 
                            sqlContext.getLimitContext().getOffsetParameterIndex(), sqlContext.getLimitContext().getRowCountParameterIndex()));
        }
    }
}
