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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.parser.antler.utils.DDLParserUtils;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.parser.antlr.mysql.MySQLDDLLexer;
import io.shardingsphere.parser.antlr.mysql.MySQLDDLParser;

/**
 * MySQL DDL Parser.
 *
 */
public class MySQLDDLParserDelegate extends AbstractDDLParser {

    public MySQLDDLParserDelegate(final ShardingRule rule, final String input) {
        super(rule, input);
    }

    /**
     * Get DDL root node of syntax tree.
     * 
     * @return DDL root node of syntax tree
     */
    @Override
    public ParseTree getRootNode() {
        CharStream stream = CharStreams.fromString(this.getInput());
        MySQLDDLLexer lexer = new MySQLDDLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLDDLParser parser = new MySQLDDLParser(tokens);
        ParseTree ddlRootNode = parser.execute();
        System.out.println(ddlRootNode.toStringTree(parser));
        return ddlRootNode;
    }

    /**
     * Parse create table.
     *
     * @param statement
     *            parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    @Override
    public void parseCreateTable(final CreateTableStatement statement, final ParseTree ddlRootNode) {
        DDLParserUtils.parseTable(statement, ddlRootNode);
        DDLParserUtils.parseCreateDefinition(statement, ddlRootNode);
    }

    /**
     * Parse alter table.
     *
     * @param statement
     *            parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    @Override
    public void parseAlterTable(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        DDLParserUtils.parseTable(statement, ddlRootNode);
        DDLParserUtils.parseRenameTable(statement, ddlRootNode);
        DDLParserUtils.parseAddColumn(statement, ddlRootNode);
        DDLParserUtils.parseModifyColumn(statement, ddlRootNode);
        DDLParserUtils.parseDropColumn(statement, ddlRootNode);
        DDLParserUtils.parseAddIndex(statement, ddlRootNode);
        DDLParserUtils.parseRenameIndex(statement, ddlRootNode);
        DDLParserUtils.parseDropIndex(statement, ddlRootNode);
        DDLParserUtils.parseAddPrimaryKey(statement, ddlRootNode);
        DDLParserUtils.parseDropPrimaryKey(statement, ddlRootNode);
    }

    /**
     * Parse create index.
     *
     * @param statement
     *            parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    @Override
    protected void parseCreateIndex(final DDLStatement statement, final ParseTree ddlRootNode) {
        DDLParserUtils.parseTable(statement, ddlRootNode);
        DDLParserUtils.parseTableIndexNode(statement, ddlRootNode);
    }

    /**
     * Parse alter index.
     *
     * @param statement
     *            parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    @Override
    protected void parseAlterIndex(final DDLStatement statement, final ParseTree ddlRootNode) {
        DDLParserUtils.parseTable(statement, ddlRootNode);
        DDLParserUtils.parseTableIndexNode(statement, ddlRootNode);
    }

    /**
     * Parse drop index.
     *
     * @param statement
     *            parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    @Override
    protected void parseDropIndex(final DDLStatement statement, final ParseTree ddlRootNode) {
        DDLParserUtils.parseTable(statement, ddlRootNode);
        DDLParserUtils.parseTableIndexNode(statement, ddlRootNode);
    }
}
