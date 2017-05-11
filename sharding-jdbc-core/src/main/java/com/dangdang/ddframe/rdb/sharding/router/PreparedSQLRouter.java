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

package com.dangdang.ddframe.rdb.sharding.router;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 预解析功能的SQL路由器.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class PreparedSQLRouter {
    
    private final String logicSql;
    
    private final SQLRouteEngine engine;
    
    private final ShardingRule shardingRule;
    
    private SQLContext sqlContext;
    
    /**
     * 使用参数进行SQL路由.
     * 当第一次路由时进行SQL解析,之后的路由复用第一次的解析结果.
     * 
     * @param parameters SQL中的参数
     * @return 路由结果
     */
    public SQLRouteResult route(final List<Object> parameters) {
        if (null == sqlContext) {
            sqlContext = engine.parseSQL(logicSql, parameters);
        } else {
            List<Number> generatedIds = generateId();
            parameters.addAll(generatedIds);
        }
        // TODO 提炼至rewrite模块
        setLimit(parameters);
        return engine.routeSQL(logicSql, sqlContext, parameters);
    }
    
    private void setLimit(final List<Object> parameters) {
        if (null == sqlContext.getLimitContext()) {
            return;
        }
        int offset = -1 == sqlContext.getLimitContext().getOffsetParameterIndex()
                ? sqlContext.getLimitContext().getOffset() : (int) parameters.get(sqlContext.getLimitContext().getOffsetParameterIndex());
        int rowCount = -1 == sqlContext.getLimitContext().getRowCountParameterIndex()
                ? sqlContext.getLimitContext().getRowCount() : (int) parameters.get(sqlContext.getLimitContext().getRowCountParameterIndex());
        sqlContext.setLimitContext(new LimitContext(offset, rowCount, sqlContext.getLimitContext().getOffsetParameterIndex(), sqlContext.getLimitContext().getRowCountParameterIndex()));
        if (offset < 0 || rowCount < 0) {
            throw new SQLParsingException("LIMIT offset and row count can not be a negative value.");
        }
    }
    
    private List<Number> generateId() {
        if (!(sqlContext instanceof InsertSQLContext)) {
            return Collections.emptyList();
        }
        Optional<TableRule> tableRuleOptional = shardingRule.tryFindTableRule(sqlContext.getTables().iterator().next().getName());
        if (!tableRuleOptional.isPresent()) {
            return Collections.emptyList();
        }
        TableRule tableRule = tableRuleOptional.get();
        GeneratedKeyContext generatedKeyContext = ((InsertSQLContext) sqlContext).getGeneratedKeyContext();
        List<Number> result = new ArrayList<>(generatedKeyContext.getColumns().size());
        for (String each : generatedKeyContext.getColumns()) {
            Number generatedId = tableRule.generateId(each);
            result.add(generatedId);
            generatedKeyContext.putValue(each, generatedId);
        }
        return result;
    }
}

