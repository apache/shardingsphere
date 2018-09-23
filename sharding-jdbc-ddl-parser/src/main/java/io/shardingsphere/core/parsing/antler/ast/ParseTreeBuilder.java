package io.shardingsphere.core.parsing.antler.ast;

import org.antlr.v4.runtime.ParserRuleContext;

public interface ParseTreeBuilder {
    ParserRuleContext parse(final String input);
}
