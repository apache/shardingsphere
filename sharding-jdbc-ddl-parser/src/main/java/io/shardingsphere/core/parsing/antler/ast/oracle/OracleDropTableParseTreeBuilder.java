package io.shardingsphere.core.parsing.antler.ast.oracle;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.oracle.OracleAdvancedDropTableParser;
import io.shardingsphere.parser.antlr.oracle.OracleDropTableLexer;

public class OracleDropTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new OracleDropTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new OracleAdvancedDropTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        OracleAdvancedDropTableParser dropTableParser = (OracleAdvancedDropTableParser) parser;
        return dropTableParser.dropTable();
    }

}
