package org.apache.shardingsphere.infra.optimize.util;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.optimize.converter.SqlNodeConverter;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * Facade for SQL parser. Calcite sql parse will be used, before the converter of ShardingSphere to Calcite ast is ready.
 */
public class SqlParserFacade {
    
    static ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
            new MySQLDatabaseType()));
    
    public static SqlNode parse(final String sql) {
        SqlNode sqlNode = null;
        // sqlNode = parseWithSs(sql);
        SqlParser parser = SqlParser.create(sql, SqlParser.config().withLex(Lex.MYSQL));
        try {
            sqlNode = parser.parseQuery();
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
        return sqlNode;
    }
    
    private static SqlNode parseWithSs(String sql) {
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        if(optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
