/**
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

import java.util.Collection;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.db2.parser.DB2StatementParser;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.VisitorLogProxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL解析器工厂.
 * 
 * @author gaohongtao, zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SQLParserFactory {
    
    /**
     * 创建解析器引擎对象.
     * 
     * @param databaseType 数据库类型
     * @param sql SQL语句
     * @param parameters SQL中参数的值
     * @param shardingColumns 分片列名称集合
     * @return 解析器引擎对象
     * @throws SQLParserException SQL解析异常
     */
    public static SQLParseEngine create(final DatabaseType databaseType, final String sql, final List<Object> parameters, final Collection<String> shardingColumns) throws SQLParserException {
        log.debug("Logic SQL: {}", sql);
        SQLStatement sqlStatement = getSQLStatementParser(databaseType, sql).parseStatement();
        log.trace("Get {} SQL Statement", sqlStatement.getClass().getName());
        return new SQLParseEngine(sqlStatement, parameters, getSQLVisitor(databaseType, sqlStatement), shardingColumns);
    }
    
    private static SQLStatementParser getSQLStatementParser(final DatabaseType databaseType, final String sql) {
        switch (databaseType) {
            case H2: 
            case MySQL: 
                return new MySqlStatementParser(sql);
            case Oracle: 
                return new OracleStatementParser(sql);
            case SQLServer: 
                return new SQLServerStatementParser(sql);
            case DB2: 
                return new DB2StatementParser(sql);
            default: 
                throw new UnsupportedOperationException(String.format("Cannot support database type [%s]", databaseType));
        }
    }
    
    private static SQLASTOutputVisitor getSQLVisitor(final DatabaseType databaseType, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLSelectStatement) {
            return VisitorLogProxy.enhance(SQLVisitorRegistry.getSelectVistor(databaseType));
        }
        if (sqlStatement instanceof SQLInsertStatement) {
            return VisitorLogProxy.enhance(SQLVisitorRegistry.getInsertVistor(databaseType));
        }
        if (sqlStatement instanceof SQLUpdateStatement) {
            return VisitorLogProxy.enhance(SQLVisitorRegistry.getUpdateVistor(databaseType));
        }
        if (sqlStatement instanceof SQLDeleteStatement) {
            return VisitorLogProxy.enhance(SQLVisitorRegistry.getDeleteVistor(databaseType));
        }
        throw new SQLParserException("Unsupported SQL statement: [%s]", sqlStatement);
    }
}
