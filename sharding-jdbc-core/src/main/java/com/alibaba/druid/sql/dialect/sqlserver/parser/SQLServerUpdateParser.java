package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerOutput;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerUpdateStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * SQLServer Update语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerUpdateParser extends AbstractUpdateParser {
    
    public SQLServerUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected SQLServerUpdateStatement createUpdateStatement() {
        return new SQLServerUpdateStatement();
    }
    
    protected void parseCustomizedParserBetweenUpdateAndTable(final AbstractSQLUpdateStatement updateStatement) {
        SQLServerTop top = ((SQLServerExprParser) getExprParser()).parseTop();
        if (null != top) {
            ((SQLServerUpdateStatement) updateStatement).setTop(top);
        }
    }
    
    @Override
    protected void parseCustomizedParserBetweenSetAndWhere(final AbstractSQLUpdateStatement updateStatement) {
        SQLServerOutput output = ((SQLServerExprParser) getExprParser()).parserOutput();
        if (null != output) {
            ((SQLServerUpdateStatement) updateStatement).setOutput(output);
        }
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            ((SQLServerUpdateStatement) updateStatement).setFrom(getExprParser().createSelectParser().parseTableSource());
        }
    }
}
