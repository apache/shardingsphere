package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.lexer.Token;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Insert语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractInsertParser extends SQLParser {
    
    @Getter
    private final SQLExprParser exprParser;
    
    public AbstractInsertParser(final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
    }
    
    /**
     * 解析Insert语句.
     * 
     * @return 解析结果
     */
    public final SQLStatement parse() {
        getLexer().nextToken();
        AbstractSQLInsertStatement result = createSQLInsertStatement();
        if (getUnsupportedIdentifiers().contains(getLexer().getLiterals())) {
            throw new UnsupportedOperationException(String.format("Cannot support %s for %s.", getLexer().getLiterals(), getDbType()));
        }
        parseInto(result);
        parseColumns(result);
        if (getValuesIdentifiers().contains(getLexer().getLiterals())) {
            parseValues(result);
        } else if (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.LEFT_PAREN)) {
            parseSelect(result);
        } else if (getCustomizedInsertIdentifiers().contains(getLexer().getToken().getName())) {
            parseCustomizedInsert(result);
        }
        parseAppendices(result);
        return result;
    }
    
    protected abstract AbstractSQLInsertStatement createSQLInsertStatement();
    
    protected void parseCustomizedInsert(final AbstractSQLInsertStatement sqlInsertStatement) {
    }
    
    protected Set<String> getUnsupportedIdentifiers() {
        return Collections.emptySet();
    }
    
    protected Set<String> getIdentifiersBetweenIntoAndTable() {
        return Collections.emptySet();
    }
    
    protected Set<String> getIdentifiersBetweenTableAndValues() {
        return Collections.emptySet();
    }
    
    protected Set<String> getValuesIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.VALUES.getName());
        return result;
    }
    
    protected Set<String> getCustomizedInsertIdentifiers() {
        return Collections.emptySet();
    }
    
    protected Set<String> getAppendixIdentifiers() {
        return Collections.emptySet();
    }
    
    private void parseInto(final AbstractSQLInsertStatement sqlInsertStatement) {
        parseBetweenInsertAndInfo(sqlInsertStatement);
        accept(Token.INTO);
        parseBetweenIntoAndTable(sqlInsertStatement);
        sqlInsertStatement.setTableSource(new SQLExprTableSource(exprParser.name()));
        parseBetweenTableAndValues(sqlInsertStatement);
        parseAlias(sqlInsertStatement);
    }
    
    protected void parseBetweenInsertAndInfo(final AbstractSQLInsertStatement sqlInsertStatement) {
        while (!getLexer().equalToken(Token.INTO) && !getLexer().equalToken(Token.EOF)) {
            sqlInsertStatement.getIdentifiersBetweenInsertAndInto().add(getLexer().getLiterals());
            getLexer().nextToken();
        }
    }
    
    protected void parseBetweenIntoAndTable(final AbstractSQLInsertStatement sqlInsertStatement) {
        while (getIdentifiersBetweenIntoAndTable().contains(getLexer().getLiterals())) {
            sqlInsertStatement.getIdentifiersBetweenIntoAndTable().add(getLexer().getLiterals());
            getLexer().nextToken();
        }
    }
    
    protected void parseBetweenTableAndValues(final AbstractSQLInsertStatement sqlInsertStatement) {
        while (getIdentifiersBetweenTableAndValues().contains(getLexer().getLiterals())) {
            sqlInsertStatement.getIdentifiersBetweenTableAndValues().add(getLexer().getLiterals());
            getLexer().nextToken();
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                do {
                    sqlInsertStatement.getIdentifiersBetweenTableAndValues().add(getLexer().getLiterals());
                    getLexer().nextToken();
                }
                while (!getLexer().equalToken(Token.RIGHT_PAREN) && !getLexer().equalToken(Token.EOF));
                sqlInsertStatement.getIdentifiersBetweenTableAndValues().add(getLexer().getLiterals());
                accept(Token.RIGHT_PAREN);
            }
        }
    }
    
    private void parseAlias(final AbstractSQLInsertStatement sqlInsertStatement) {
        if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            sqlInsertStatement.getTableSource().setAlias(as());
        }
        if (getLexer().equalToken(Token.IDENTIFIER) && !getValuesIdentifiers().contains(getLexer().getLiterals())) {
            sqlInsertStatement.getTableSource().setAlias(getLexer().getLiterals());
            getLexer().nextToken();
        }
    }
    
    private void parseColumns(final AbstractSQLInsertStatement sqlInsertStatement) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            sqlInsertStatement.getColumns().addAll(exprParser.exprList(sqlInsertStatement));
            accept(Token.RIGHT_PAREN);
        }
    }
    
    private void parseValues(final AbstractSQLInsertStatement sqlInsertStatement) {
        do {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            AbstractSQLInsertStatement.ValuesClause values = new AbstractSQLInsertStatement.ValuesClause();
            values.getValues().addAll(getExprParser().exprList(values));
            sqlInsertStatement.getValuesList().add(values);
            accept(Token.RIGHT_PAREN);
        }
        while (getLexer().equalToken(Token.COMMA));
    }
    
    private void parseSelect(final AbstractSQLInsertStatement sqlInsertStatement) {
        SQLSelect select = exprParser.createSelectParser().select();
        select.setParent(sqlInsertStatement);
        sqlInsertStatement.setQuery(select);
    }
    
    private void parseAppendices(final AbstractSQLInsertStatement sqlInsertStatement) {
        if (getAppendixIdentifiers().contains(getLexer().getLiterals())) {
            while (!getLexer().equalToken(Token.EOF)) {
                sqlInsertStatement.getAppendices().add(getLexer().getLiterals());
                getLexer().nextToken();
            }
        }
    }
}
