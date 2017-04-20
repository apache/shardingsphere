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
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
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
        ParseContext result = new ParseContext();
        SQLParsedResult sqlParsedResult = result.getParsedResult();
        if (sqlContext.getConditionContexts().isEmpty()) {
            sqlParsedResult.getConditionContexts().add(new ConditionContext());
        } else {
            sqlParsedResult.getConditionContexts().addAll(sqlContext.getConditionContexts());
        }
        for (TableContext each : sqlContext.getTables()) {
            sqlParsedResult.getRouteContext().getTables().add(each);
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
            if (each instanceof AggregationSelectItemContext) {
                AggregationSelectItemContext aggregationSelectItemContext = (AggregationSelectItemContext) each;
                // TODO index获取不准，考虑使用别名替换
                AggregationColumn column = new AggregationColumn(aggregationSelectItemContext.getExpression(), aggregationSelectItemContext.getAggregationType(),
                        aggregationSelectItemContext.getAlias(), aggregationSelectItemContext.getIndex());
                sqlParsedResult.getMergeContext().getAggregationColumns().add(column);
                if (AggregationColumn.AggregationType.AVG.equals(aggregationSelectItemContext.getAggregationType())) {
                    AggregationSelectItemContext aggregationSelectItemContext1 = aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().get(0);
                    AggregationColumn column1 = new AggregationColumn(aggregationSelectItemContext1.getExpression(), aggregationSelectItemContext1.getAggregationType(),
                            aggregationSelectItemContext1.getAlias(), aggregationSelectItemContext1.getIndex());
                    column.getDerivedColumns().add(column1);
                    AggregationSelectItemContext aggregationSelectItemContext2 = aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().get(1);
                    AggregationColumn column2 = new AggregationColumn(aggregationSelectItemContext2.getExpression(), aggregationSelectItemContext2.getAggregationType(),
                            aggregationSelectItemContext2.getAlias(), aggregationSelectItemContext2.getIndex());
                    column.getDerivedColumns().add(column2);
                    sqlParsedResult.getMergeContext().getAggregationColumns().add(column1);
                    sqlParsedResult.getMergeContext().getAggregationColumns().add(column2);
                }
            }
        }
        
        if (!sqlContext.getGroupByContexts().isEmpty()) {
            for (GroupByContext each : sqlContext.getGroupByContexts()) {
                parseContext.getParsedResult().getMergeContext().getGroupByContexts().add(each);
            }
        }
        
        if (!sqlContext.getOrderByContexts().isEmpty()) {
            for (OrderByContext each : sqlContext.getOrderByContexts()) {
                if (each.getIndex().isPresent()) {
                    parseContext.getParsedResult().getMergeContext().getOrderByContexts().add(new OrderByContext(each.getIndex().get(), each.getOrderByType()));
                } else {
                    parseContext.getParsedResult().getMergeContext().getOrderByContexts().add(each);
                }
            }
        }
        
        if (null != sqlContext.getLimitContext()) {
            parseContext.getParsedResult().getMergeContext().setLimit(
                    new Limit(sqlContext.getLimitContext().getOffset().isPresent() ? sqlContext.getLimitContext().getOffset().get() : 0, sqlContext.getLimitContext().getRowCount(), 
                            sqlContext.getLimitContext().getOffsetParameterIndex(), sqlContext.getLimitContext().getRowCountParameterIndex()));
        }
    }
}
