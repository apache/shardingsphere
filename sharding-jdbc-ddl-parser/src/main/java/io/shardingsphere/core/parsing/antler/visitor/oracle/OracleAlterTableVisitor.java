package io.shardingsphere.core.parsing.antler.visitor.oracle;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
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
        VisitorUtils.parseTable(alterStatement, rootNode);
        VisitorUtils.parseRenameTable(alterStatement, rootNode);
        VisitorUtils.parseAddColumn(alterStatement, rootNode);
        VisitorUtils.parseModifyColumn(alterStatement, rootNode);
        VisitorUtils.parseDropColumn(alterStatement, rootNode);
        VisitorUtils.parseAddPrimaryKey(alterStatement, rootNode);
        VisitorUtils.parseDropPrimaryKey(alterStatement, rootNode);
    }

}
