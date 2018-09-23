package io.shardingsphere.core.parsing.antler.visitor.oracle;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.DDLParserUtils;
import io.shardingsphere.core.parsing.antler.visitor.AbstractSQLVisitor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class OracleAlterTableVisitor extends AbstractSQLVisitor {

    @Override
    protected SQLStatement newStatement() {
        return new AlterTableStatement();
    }

    @Override
    protected void visit(ParserRuleContext rootNode, SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        DDLParserUtils.parseTable(alterStatement, rootNode);
        DDLParserUtils.parseRenameTable(alterStatement, rootNode);
        DDLParserUtils.parseAddColumn(alterStatement, rootNode);
        DDLParserUtils.parseModifyColumn(alterStatement, rootNode);
        DDLParserUtils.parseDropColumn(alterStatement, rootNode);
        DDLParserUtils.parseAddPrimaryKey(alterStatement, rootNode);
        DDLParserUtils.parseDropPrimaryKey(alterStatement, rootNode);
    }

}
