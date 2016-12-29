package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.lexer.Token;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractUpdateParser extends SQLParser {
    
    @Getter
    private final SQLExprParser exprParser;
    
    public AbstractUpdateParser(final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
    }
    
    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public SQLUpdateStatement parse() {
        getLexer().nextToken();
        SQLUpdateStatement result = createUpdateStatement();
        parseCustomizedParserBetweenUpdateAndTable(result);
        parseIdentifiersBetweenUpdateAndTable(result);
        result.setTableSource(exprParser.createSelectParser().parseTableSource());
        parseAlias(result);
        parseUpdateSet(result);
        parseCustomizedParserBetweenSetAndWhere(result);
        parseWhere(result);
        parseCustomizedParserAfterWhere(result);
        parseAppendices(result);
        return result;
    }
    
    protected abstract SQLUpdateStatement createUpdateStatement();
    
    protected void parseCustomizedParserBetweenUpdateAndTable(final SQLUpdateStatement updateStatement) {
    }
    
    protected void parseCustomizedParserBetweenSetAndWhere(final SQLUpdateStatement updateStatement) {
    }
    
    protected Set<String> getIdentifiersBetweenUpdateAndTable() {
        return Collections.emptySet();
    }
    
    protected void parseAlias(final SQLUpdateStatement updateStatement) {
    }
    
    protected void parseCustomizedParserAfterWhere(final SQLUpdateStatement updateStatement) {
    }
    
    protected Set<String> getAppendixIdentifiers() {
        return Collections.emptySet();
    }
    
    private void parseIdentifiersBetweenUpdateAndTable(final SQLUpdateStatement sqlUpdateStatement) {
        while (getIdentifiersBetweenUpdateAndTable().contains(getLexer().getLiterals())) {
            sqlUpdateStatement.getIdentifiersBetweenUpdateAndTable().add(getLexer().getLiterals());
            getLexer().nextToken();
        }
    }
    
    private void parseUpdateSet(final SQLUpdateStatement updateStatement) {
        accept(Token.SET);
        while (true) {
            updateStatement.addItem(exprParser.parseUpdateSetItem());
            if (!getLexer().equalToken(Token.COMMA)) {
                break;
            }
            getLexer().nextToken();
        }
    }
    
    private void parseWhere(final SQLUpdateStatement sqlUpdateStatement) {
        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            sqlUpdateStatement.setWhere(exprParser.expr());
        }
    }
    
    private void parseAppendices(final SQLUpdateStatement sqlInsertStatement) {
        if (getAppendixIdentifiers().contains(getLexer().getLiterals())) {
            while (!getLexer().equalToken(Token.EOF)) {
                sqlInsertStatement.getAppendices().add(getLexer().getLiterals());
                getLexer().nextToken();
            }
        }
    }
}
