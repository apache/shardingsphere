package io.shardingsphere.core.parsing.antler.visitor;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public interface SQLVisitor {
    SQLStatement visit(final ParserRuleContext rootNode);
}
