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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.google.common.base.Optional;

import java.util.List;

/**
 * SQL上下文.
 *
 * @author zhangliang
 */
public interface SQLContext {
    
    /**
     * 获取SQL语句类型.
     *
     * @return SQL语句类型
     */
    SQLType getType();
    
    /**
     * 获取表解析对象集合.
     * 
     * @return 表解析对象集合
     */
    List<TableContext> getTables();
    
    /**
     * 获取条件对象上下文.
     * 
     * @return 条件对象上下文
     */
    ConditionContext getConditionContext();
    
    /**
     * 设置条件对象上下文.
     *
     * @param conditionContext  条件对象上下文
     */
    void setConditionContext(ConditionContext conditionContext);
    
    /**
     * 获取列对象.
     * 
     * @param expr SQL表达式
     * @return 列对象
     */
    Optional<ShardingColumnContext> findColumn(SQLExpr expr);
    
    /**
     * 获取排序上下文集合.
     * 
     * @return 排序上下文集合
     */
    List<OrderByContext> getOrderByContexts();
    
    /**
     * 获取分组上下文集合.
     * 
     * @return 分组上下文集合
     */
    List<GroupByContext> getGroupByContexts();
    
    /**
     * 获取聚合上下文集合.
     * 
     * @return 聚合上下文集合
     */
    List<AggregationSelectItemContext> getAggregationSelectItemContexts();
    
    /**
     * 获取分页上下文.
     * 
     * @return 分页上下文
     */
    LimitContext getLimitContext();
    
    /**
     * 设置分页上下文.
     *
     * @param limitContext  分页上下文
     */
    void setLimitContext(LimitContext limitContext);
    
    /**
     * 获取SQL标记集合.
     * 
     * @return SQL标记集合
     */
    List<SQLToken> getSqlTokens();
}
