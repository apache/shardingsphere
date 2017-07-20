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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;

import java.util.List;

/**
 * SQL语句对象.
 *
 * @author zhangliang
 */
public interface SQLStatement {
    
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
    Tables getTables();
    
    /**
     * 获取条件对象集合.
     *
     * @return 条件对象集合
     */
    Conditions getConditions();
    
    /**
     * 获取SQL标记集合.
     * 
     * @return SQL标记集合
     */
    List<SQLToken> getSqlTokens();
}
