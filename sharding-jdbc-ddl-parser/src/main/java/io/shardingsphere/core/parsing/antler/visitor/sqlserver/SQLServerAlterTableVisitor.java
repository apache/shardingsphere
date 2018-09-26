package io.shardingsphere.core.parsing.antler.visitor.sqlserver;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.antler.visitor.AlterTableVisitor;
import io.shardingsphere.core.parsing.parser.token.IndexToken;

public class SQLServerAlterTableVisitor extends AlterTableVisitor {
    protected void visitPrivateTree(final AlterTableStatement statement, final ParseTree rootNode) {
        visitAddIndex(statement, rootNode);
        visitDropIndex(statement, rootNode);
        visitAddPrimaryKey(statement, rootNode);//drop primary key need use drop constraint
    }

    /**
     * Visit add index node.
     * 
     * @param statement
     *            statement parse result
     * @param ancestorNode
     *            ancestor of index node
     * @return indexName node
     */
    protected void visitAddIndex(final AlterTableStatement statement, final ParseTree ancestorNode) {
        ParserRuleContext indexDefOptionNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode,
                "addColumn");
        if (null != indexDefOptionNode) {
            ParserRuleContext indexNameNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(indexDefOptionNode,
                    "indexName");
            if (null != indexNameNode) {
                statement.getSqlTokens().add(new IndexToken(indexNameNode.getStart().getStartIndex(),
                        indexNameNode.getText(), statement.getTables().getSingleTableName()));
            }
        }
    }

    /**
     * Visit drop index node.
     * 
     * @param statement
     *            statement parse result
     * @param ancestorNode
     *            ancestor of index node
     * @return indexName node
     */
    protected void visitDropIndex(final AlterTableStatement statement, final ParseTree ancestorNode) {
        ParserRuleContext indexDefOptionNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode,
                "alterDropIndex");
        if (null != indexDefOptionNode) {
            ParserRuleContext indexNameNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(indexDefOptionNode,
                    "indexName");
            if (null != indexNameNode) {
                statement.getSqlTokens().add(new IndexToken(indexNameNode.getStart().getStartIndex(),
                        indexNameNode.getText(), statement.getTables().getSingleTableName()));
            }
        }
    }

    /**
     * Visit drop index node.
     * 
     * @param statement
     *            statement parse result
     * @param ancestorNode
     *            ancestor of index node
     * @return indexName node
     */
    protected void visitAddPrimaryKey(final AlterTableStatement statement, final ParseTree ancestorNode) {
        ParserRuleContext addColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode,
                "addColumn");

        if (null == addColumnCtx) {
            return;
        }

        List<ParseTree> alterColumnAddOptionCtxs = TreeUtils.getAllDescendantByRuleName(addColumnCtx,
                "alterColumnAddOption");

        if (null == alterColumnAddOptionCtxs) {
            return;
        }

        for (ParseTree each : alterColumnAddOptionCtxs) {
            ParserRuleContext primaryKeyNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(each,
                    "primaryKey");
            if (null == primaryKeyNode) {
                continue;
            }

            List<ParseTree> columnNameCtxs = TreeUtils.getAllDescendantByRuleName(each, "columnName");
            for (ParseTree columnNameCtx : columnNameCtxs) {
                statement.getUpdateColumns().put(columnNameCtx.getText(),
                        new ColumnDefinition(columnNameCtx.getText(), null, null, true));
            }
        }
    }
}
