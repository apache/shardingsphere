package io.shardingsphere.core.parsing.antler.visitor.oracle;

import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.antler.visitor.AlterTableVisitor;

public class OracleAlterTableVisitor extends AlterTableVisitor {
    @Override
    protected void visitPrivateTree(AlterTableStatement statement, ParseTree rootNode) {
        VisitorUtils.visitOracleModifyColumn(statement, rootNode);
        VisitorUtils.visitRenameColumn(statement, rootNode);
        VisitorUtils.visitAddPrimaryKey(statement, rootNode, "addConstraintClause");
        VisitorUtils.visitOracleDropPrimaryKey(statement, rootNode);
    }

}
