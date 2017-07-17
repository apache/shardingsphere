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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.delete;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerDeleteParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Delete语句解析器工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteParserFactory {
    
    /**
     * 创建Delete语句解析器.
     * 
     * @param sqlParser SQL解析器
     * @return Delete语句解析器
     */
    public static AbstractDeleteParser newInstance(final SQLParser sqlParser) {
        if (sqlParser instanceof MySQLParser) {
            return new MySQLDeleteParser(sqlParser);
        }
        if (sqlParser instanceof OracleParser) {
            return new OracleDeleteParser(sqlParser);
        }
        if (sqlParser instanceof SQLServerParser) {
            return new SQLServerDeleteParser(sqlParser);
        }
        if (sqlParser instanceof PostgreSQLParser) {
            return new PostgreSQLDeleteParser(sqlParser);
        }
        throw new UnsupportedOperationException(String.format("Cannot support sqlParser class [%s].", sqlParser.getClass()));
    } 
}
