package io.shardingsphere.core.parsing.antler.parser.factory;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antler.VisitorManager;
import io.shardingsphere.core.parsing.antler.visitor.SQLVisitor;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;

public class StatementFactory {
    public static SQLStatement getStatement(final DatabaseType dbType, final TokenType tokenType,
            final ShardingRule shardingRule, final LexerEngine lexerEngine) {

        ParserRuleContext rootNode = ParseTreeFactory.getTableDDLParser(dbType, tokenType, shardingRule, lexerEngine);
        if (rootNode != null) {
            String commandName = getCommandName(rootNode);
            SQLVisitor visitor = VisitorManager.getInstance().getVisitor(dbType, commandName);
            if (null != visitor) {
                return visitor.visit(rootNode);
            }
        }

        return null;
    }

    private static String getCommandName(ParserRuleContext node) {
        String name = node.getClass().getSimpleName();
        int pos = name.indexOf("Context");
        if (pos > 0) {
            return name.substring(0, pos);
        }

        return name;
    }
}
