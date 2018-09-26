package io.shardingsphere.core.parsing.antler.visitor.oracle;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.antler.visitor.AlterTableVisitor;

public class OracleAlterTableVisitor extends AlterTableVisitor {
    @Override
    protected void visitPrivateTree(AlterTableStatement statement, ParseTree rootNode) {
        visitModifyColumn(statement, rootNode);
        visitRenameColumn(statement, rootNode);
        visitAddPrimaryKey(statement, rootNode);
        visitDropPrimaryKey(statement, rootNode);
    }

    /**
     * Visit alter table modify column nodes.
     * 
     * @param statement
     *            statement parse result
     * @param rootNode
     *            Root node of syntax tree
     */
    protected void visitModifyColumn(final AlterTableStatement statement, final ParseTree rootNode) {
        ParserRuleContext modifyColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(rootNode,
                "modifyColumn");
        if (null == modifyColumnCtx) {
            return;
        }

        List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(modifyColumnCtx, "modifyColProperties");
        if (null == columnNodes) {
            return;
        }

        for (final ParseTree each : columnNodes) {
            // it`s not columndefinition, but can call this method
            ColumnDefinition column = VisitorUtils.parseColumnDefinition(each);
            if (null != column) {
                statement.getAddColumns().add(column);
            }
        }
    }

    /**
     * Visit alter table rename column nodes.
     * 
     * @param statement
     *            statement parse result
     * @param rootNode
     *            Root node of syntax tree
     */
    protected void visitRenameColumn(final AlterTableStatement statement, final ParseTree rootNode) {
        ParserRuleContext modifyColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(rootNode,
                "renameColumn");
        if (null == modifyColumnCtx) {
            return;
        }

        List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(modifyColumnCtx, "columnName");
        if (null == columnNodes || columnNodes.size() != 2) {
            return;
        }

        String oldName = columnNodes.get(0).getText();
        String newName = columnNodes.get(1).getText();
        ColumnDefinition oldDefinition = statement.getUpdateColumns().remove(oldName);
        if (null != oldDefinition) {
            oldDefinition.setName(newName);
        } else {
            oldDefinition = new ColumnDefinition(newName, null, null, false);
        }

        statement.getUpdateColumns().put(newName, oldDefinition);
    }

    /**
     * Visit alter table modify column nodes.
     * 
     * @param statement
     *            statement parse result
     * @param rootNode
     *            Root node of syntax tree
     */
    protected void visitAddPrimaryKey(final AlterTableStatement statement, final ParseTree rootNode) {
        ParserRuleContext modifyColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(rootNode,
                "addConstraintClause");
        if (null == modifyColumnCtx) {
            return;
        }

        ParseTree primaryKeyCtx = TreeUtils.getFirstChildByRuleName(modifyColumnCtx, "primaryKey");
        if (null == primaryKeyCtx) {
            return;
        }

        List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(modifyColumnCtx, "columnName");
        if (null == columnNodes) {
            return;
        }
        for (final ParseTree each : columnNodes) {
            String columnName = each.getText();
            statement.getUpdateColumns().put(columnName, new ColumnDefinition(columnName, null, null, true));
        }
    }

    /**
     * Parse drop primary key.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    protected void visitDropPrimaryKey(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParseTree dropPrimaryKeyCtx = TreeUtils.getFirstChildByRuleName(ddlRootNode, "dropConstraintClause");
        if (null != dropPrimaryKeyCtx) {
            ParseTree primaryKeyCtx = TreeUtils.getFirstChildByRuleName(dropPrimaryKeyCtx, "primaryKey");
            if (null != primaryKeyCtx) {
                statement.setDropPrimaryKey(true);
            }
        }
    }

}
