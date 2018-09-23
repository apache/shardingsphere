package io.shardingsphere.core.parsing.antler.ast;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

public abstract class AbstractParseTreeBuilder implements ParseTreeBuilder {

    @Override
    public ParserRuleContext parse(final String input) {
        CharStream stream = CharStreams.fromString(input);
        Lexer lexer = newLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        Parser parser = newParser(tokenStream);
        return getParserTree(parser);
    }

    protected abstract Lexer newLexer(final CharStream stream);

    protected abstract Parser newParser(final TokenStream tokenStream);

    protected abstract ParserRuleContext getParserTree(final Parser parser);

}
