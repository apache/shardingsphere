package io.shardingsphere.core.parsing.antler.visitor.postgre;

import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.antler.visitor.AlterTableVisitor;

public class PostgreAlterTableVisitor extends AlterTableVisitor {

    @Override
    protected void visitPrivateTree(final AlterTableStatement statement, final ParseTree rootNode) {
        VisitorUtils.visitAddPrimaryKey(statement, rootNode, "alterTableAddConstraint");
        VisitorUtils.visitRenameColumn(statement, rootNode);
        VisitorUtils.visitModifyColumn(statement, rootNode);
    }
}
