package io.shardingsphere.core.parsing.antler.ast.mysql;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.mysql.MySQLAdvancedAlterTableParser;
import io.shardingsphere.parser.antlr.mysql.MySQLAlterTableLexer;

public class MySQLAlterTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new MySQLAlterTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new MySQLAdvancedAlterTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        MySQLAdvancedAlterTableParser alterTableParser = (MySQLAdvancedAlterTableParser)parser;
        return alterTableParser.alterTable();
    }

    

}
