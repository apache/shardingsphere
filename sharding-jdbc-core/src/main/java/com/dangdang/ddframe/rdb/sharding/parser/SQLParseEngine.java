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

import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 不包含OR语句的SQL构建器解析.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
// TODO 与SQLParserEngine合一
public final class SQLParseEngine {
    
    private final SQLContext sqlContext;
    
    /**
     *  解析SQL.
     * 
     * @return SQL解析结果
     */
    public SQLParsedResult parse() {
        SQLParsedResult result = getSQLParsedResult(sqlContext);
        if (sqlContext instanceof SelectSQLContext) {
            parseSelect(result, (SelectSQLContext) sqlContext);
        } else if (sqlContext instanceof InsertSQLContext) {
            parseInsert(result, (InsertSQLContext) sqlContext);
        }
        result.getRouteContext().setSqlBuilder(sqlContext.toSqlBuilder());
        return result;
    }
    
    private SQLParsedResult getSQLParsedResult(final SQLContext sqlContext) {
        SQLParsedResult result = new SQLParsedResult();
        if (sqlContext.getConditionContexts().isEmpty()) {
            result.getConditionContexts().add(new ConditionContext());
        } else {
            result.getConditionContexts().addAll(sqlContext.getConditionContexts());
        }
        for (TableContext each : sqlContext.getTables()) {
            result.getRouteContext().getTables().add(each);
        }
        result.getRouteContext().setSqlStatementType(sqlContext.getType());
        return result;
    }
    
    private void parseInsert(final SQLParsedResult sqlParsedResult, final InsertSQLContext sqlContext) {
        sqlParsedResult.setGeneratedKeyContext(sqlContext.getGeneratedKeyContext());
    }
    
    private void parseSelect(final SQLParsedResult sqlParsedResult, final SelectSQLContext sqlContext) {
        for (SelectItemContext each : sqlContext.getItemContexts()) {
            if (each instanceof AggregationSelectItemContext) {
                AggregationSelectItemContext aggregationSelectItemContext = (AggregationSelectItemContext) each;
                // TODO index获取不准，考虑使用别名替换
                sqlParsedResult.getMergeContext().getAggregationColumns().add(aggregationSelectItemContext);
                if (AggregationType.AVG.equals(aggregationSelectItemContext.getAggregationType())) {
                    AggregationSelectItemContext aggregationSelectItemContext1 = aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().get(0);
                    AggregationSelectItemContext column1 = new AggregationSelectItemContext(aggregationSelectItemContext1.getInnerExpression(), aggregationSelectItemContext1.getAlias(), 
                            aggregationSelectItemContext1.getIndex(), aggregationSelectItemContext1.getAggregationType());
                    AggregationSelectItemContext aggregationSelectItemContext2 = aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().get(1);
                    AggregationSelectItemContext column2 = new AggregationSelectItemContext(aggregationSelectItemContext2.getInnerExpression(), aggregationSelectItemContext2.getAlias(), 
                            aggregationSelectItemContext2.getIndex(), aggregationSelectItemContext2.getAggregationType());
                    sqlParsedResult.getMergeContext().getAggregationColumns().add(column1);
                    sqlParsedResult.getMergeContext().getAggregationColumns().add(column2);
                }
            }
        }
        
        if (!sqlContext.getGroupByContexts().isEmpty()) {
            for (GroupByContext each : sqlContext.getGroupByContexts()) {
                sqlParsedResult.getMergeContext().getGroupByContexts().add(each);
            }
        }
        
        if (!sqlContext.getOrderByContexts().isEmpty()) {
            for (OrderByContext each : sqlContext.getOrderByContexts()) {
                if (each.getIndex().isPresent()) {
                    sqlParsedResult.getMergeContext().getOrderByContexts().add(new OrderByContext(each.getIndex().get(), each.getOrderByType()));
                } else {
                    sqlParsedResult.getMergeContext().getOrderByContexts().add(each);
                }
            }
        }
        
        if (null != sqlContext.getLimitContext()) {
            sqlParsedResult.getMergeContext().setLimit(
                    new Limit(sqlContext.getLimitContext().getOffset().isPresent() ? sqlContext.getLimitContext().getOffset().get() : 0, sqlContext.getLimitContext().getRowCount(), 
                            sqlContext.getLimitContext().getOffsetParameterIndex(), sqlContext.getLimitContext().getRowCountParameterIndex()));
        }
    }
}
