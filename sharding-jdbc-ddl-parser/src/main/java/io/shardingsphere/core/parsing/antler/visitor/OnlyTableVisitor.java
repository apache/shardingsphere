package io.shardingsphere.core.parsing.antler.visitor;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;

public class OnlyTableVisitor extends AbstractSQLVisitor {

    @Override
    protected SQLStatement newStatement() {
        return new DDLStatement();
    }

    @Override
    protected void visit(final ParserRuleContext rootNode, final SQLStatement statement) {
        List<ParseTree> tableNameNodes = TreeUtils.getAllDescendantByRuleName(rootNode, "tableName");
        if (null != tableNameNodes) {
            for (ParseTree each : tableNameNodes) {
                VisitorUtils.parseTableNode(statement, (ParserRuleContext) each);
            }
        }
    }

}
