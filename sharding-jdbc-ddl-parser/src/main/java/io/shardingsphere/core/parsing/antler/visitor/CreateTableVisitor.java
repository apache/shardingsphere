package io.shardingsphere.core.parsing.antler.visitor;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.utils.DDLParserUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

public class CreateTableVisitor extends AbstractSQLVisitor {

    @Override
    protected SQLStatement newStatement() {
        return new CreateTableStatement();
    }

    @Override
    protected void visit(final ParserRuleContext rootNode, final SQLStatement statement) {
        DDLParserUtils.parseTable(statement, rootNode);
        DDLParserUtils.parseColumnDefinition((CreateTableStatement)statement, rootNode);
    }
}
