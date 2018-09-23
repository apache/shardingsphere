package io.shardingsphere.core.parsing.antler.visitor;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public abstract class AbstractSQLVisitor implements SQLVisitor {
    @Override
    public SQLStatement visit(final ParserRuleContext rootNode) {
        SQLStatement statement = newStatement();
        visit(rootNode, statement);
        return statement;
    }

    protected abstract SQLStatement newStatement();

    protected abstract void visit(final ParserRuleContext rootNode, final SQLStatement statement);
}
