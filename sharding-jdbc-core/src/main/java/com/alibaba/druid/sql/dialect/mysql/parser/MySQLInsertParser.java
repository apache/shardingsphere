package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractInsertParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

import java.util.Set;
import java.util.TreeSet;

/**
 * MySQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLInsertParser extends AbstractInsertParser {
    
    public MySQLInsertParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void parseCustomizedInsert(final AbstractSQLInsertStatement sqlInsertStatement) {
        parseInsertSet((MySqlInsertStatement) sqlInsertStatement);
    }
    
    private void parseInsertSet(final MySqlInsertStatement mySqlInsertStatement) {
        AbstractSQLInsertStatement.ValuesClause values = new AbstractSQLInsertStatement.ValuesClause();
        mySqlInsertStatement.getValuesList().add(values);
        do {
            getLexer().nextToken();
            mySqlInsertStatement.getColumns().add(getExprParser().name());
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
            } else {
                accept(Token.COLON_EQ);
            }
            values.getValues().add(getExprParser().expr());
        } while (getLexer().equalToken(Token.COMMA));
    }
    
    @Override
    protected MySqlInsertStatement createSQLInsertStatement() {
        return new MySqlInsertStatement();
    }
    
    @Override
    protected void parseBetweenTableAndValues(final AbstractSQLInsertStatement sqlInsertStatement) {
        ((MySqlInsertStatement) sqlInsertStatement).getPartitionNames().addAll(((MySqlExprParser) getExprParser()).parsePartition());
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenTableAndValues() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.PARTITION.getName());
        return result;
    }
    
    @Override
    protected Set<String> getValuesIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.VALUES.getName());
        result.add("VALUE");
        return result;
    }
    
    @Override
    protected Set<String> getCustomizedInsertIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.SET.getName());
        return result;
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.ON.getName());
        return result;
    }
}
