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

import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;

import java.util.Collection;
import java.util.List;

/**
 * SQL上下文.
 *
 * @author zhangliang
 */
public interface SQLContext {
    
    /**
     * 获取表解析对象集合.
     * 
     * @return 表解析对象集合
     */
    List<TableContext> getTables();
    
    /**
     * 获取条件对象上下文集合.
     * 
     * @return 条件对象上下文集合
     */
    Collection<ConditionContext> getConditionContexts();
    
    /**
     * 获取SQL语言标记对象集合.
     * 
     * @return SQL语言标记对象集合
     */
    List<SQLToken> getSqlTokens();
    
    /**
     * 生成SQL构建器.
     *
     * @return SQL构建器
     */
    SQLBuilder toSqlBuilder();
    
    /**
     * 获取SQL语句类型.
     * 
     * @return SQL语句类型
     */
    SQLStatementType getType();
}
