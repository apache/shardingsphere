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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser.MySQLDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.parser.OracleDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.parser.PostgreSQLDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.parser.SQLServerDeleteParser;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

/**
 * Delete语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLDeleteParserFactory {
    
    /**
     * 创建Delete语句解析器.
     * 
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Delete语句解析器
     */
    public static AbstractDeleteParser newInstance(final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLDeleteParser(exprParser);
            case Oracle:
                return new OracleDeleteParser(exprParser);
            case SQLServer:
                return new SQLServerDeleteParser(exprParser);
            case PostgreSQL:
                return new PostgreSQLDeleteParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
