/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.antler.sql.ddl;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.shardingsphere.core.parsing.parser.antler.utils.DDLParserUtils;
import io.shardingsphere.core.parsing.parser.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class AbstractDDLParser implements SQLParser {
    private final ShardingRule rule;
    
    private final String input;

    /**
     * Parse DDL SQL.
     *
     * @return DDL SQL statement
     */
    public SQLStatement parse() {
        ParseTree ddlRootNode = getDDLRoot();
        return createStatement(ddlRootNode);
    }

    private ParseTree getDDLRoot() {
        ParseTree rootNode = getRootNode();
        ParseTree ddlRootNode;
        if (rootNode == null) {
            throw new RuntimeException("");
        }

        ddlRootNode = rootNode.getChild(0);
        if (ddlRootNode == null) {
            throw new RuntimeException("");
        }

        return ddlRootNode;
    }

    private DDLStatement createStatement(final ParseTree ddlRootNode) {
        DDLStatement statement;
        String name = ddlRootNode.getClass().getSimpleName();
        int pos = name.indexOf("Context");
        name = name.substring(0, pos);
        if (DDLType.CREATETABLE.name().equalsIgnoreCase(name)) {
            statement = new CreateTableStatement();
            parseCreateTable((CreateTableStatement) statement, ddlRootNode);
        } else if (DDLType.ALTERTABLE.name().equalsIgnoreCase(name)) {
            statement = new AlterTableStatement();
            parseAlterTable((AlterTableStatement) statement, ddlRootNode);
        } else if (DDLType.DROPTABLE.name().equalsIgnoreCase(name)) {
            statement = new DDLStatement();
            parseDropTable(statement, ddlRootNode);
        } else if (DDLType.TRUNCATETABLE.name().equalsIgnoreCase(name)) {
            statement = new DDLStatement();
            parseTruncateTable(statement, ddlRootNode);
        } else if (DDLType.CREATEINDEX.name().equalsIgnoreCase(name)) {
            statement = new DDLStatement();
            parseCreateIndex(statement, ddlRootNode);
        } else if (DDLType.ALTERINDEX.name().equalsIgnoreCase(name)) {
            statement = new DDLStatement();
            parseAlterIndex(statement, ddlRootNode);
        } else if (DDLType.DROPINDEX.name().equalsIgnoreCase(name)) {
            statement = new DDLStatement();
            parseDropIndex(statement, ddlRootNode);
        } else {
            statement = new DDLStatement();
        }

        return statement;
    }

    /** Parse drop table statement.
     *
     * @param statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    protected void parseDropTable(final DDLStatement statement, final ParseTree ddlRootNode) {
        List<ParseTree> tableNameNodes = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "tableName");
        if (null != tableNameNodes) {
            for (ParseTree each : tableNameNodes) {
                DDLParserUtils.parseTableNode(statement, (ParserRuleContext) each);
            }
        }
    }

    /**Parse truncate table.
     *
     * @param statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    protected void parseTruncateTable(final DDLStatement statement, final ParseTree ddlRootNode) {
        ParseTree secondChild = ddlRootNode.getChild(1);
        int tableNameInd = 1;
        if (secondChild instanceof TerminalNode) {
            tableNameInd = 2;
        }

        DDLParserUtils.parseTableNode(statement, (ParserRuleContext) ddlRootNode.getChild(tableNameInd));
    }

    /** Get DDL root node of syntax tree.
     * 
     * @return DDL root node of syntax tree
     */
    public abstract ParseTree getRootNode();

    protected abstract void parseCreateTable(CreateTableStatement statement, ParseTree ddlRootNode);

    protected abstract void parseAlterTable(AlterTableStatement statement, ParseTree ddlRootNode);

    protected abstract void parseCreateIndex(DDLStatement statement, ParseTree ddlRootNode);

    protected abstract void parseAlterIndex(DDLStatement statement, ParseTree ddlRootNode);

    protected abstract void parseDropIndex(DDLStatement statement, ParseTree ddlRootNode);

}
