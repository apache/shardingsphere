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

package com.dangdang.ddframe.rdb.sharding.parser.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.contstant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLExpr;
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
    Optional<Condition.Column> findColumn(SQLExpr expr);
    
    /**
     * 获取SQL构建器上下文.
     * 
     * @return SQL构建器上下文
     */
    SQLBuilderContext getSqlBuilderContext();
    
    /**
     * 设置SQL构建器上下文.
     *
     * @param sqlBuilderContext SQL构建器上下文
     */
    void setSqlBuilderContext(SQLBuilderContext sqlBuilderContext);
}
