package io.shardingsphere.core.parsing.antler.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public abstract class AlterTableVisitor extends AbstractSQLVisitor {

    @Override
    protected SQLStatement newStatement() {
        return new AlterTableStatement();
    }

    @Override
    protected final void visit(final ParserRuleContext rootNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        VisitorUtils.parseTable(statement, rootNode);
        if (VisitorUtils.parseRenameTable(alterStatement, rootNode)) {
            return;
        }

        VisitorUtils.parseAddColumn(alterStatement, rootNode);
        VisitorUtils.visitDropColumn(alterStatement, rootNode);

        visitPrivateTree(alterStatement, rootNode);
    }

    /**
     * Visit private ast nodes.
     * 
     * @param statement
     *            statement parse result
     * @param rootNode
     *            Root node of syntax tree
     */
    protected abstract void visitPrivateTree(final AlterTableStatement statement, final ParseTree rootNode);

}
