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

package io.shardingsphere.core.parsing.parser.sql.ddl;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.antlr.utils.DDLParserUtils;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.parser.antlr.mysql.MySQLDDLLexer;
import io.shardingsphere.parser.antlr.mysql.MySQLDDLParser;

public class MySQLDDLParserDelegate extends AbstractDDLParser {

	public MySQLDDLParserDelegate(ShardingRule rule, String input) {
		super(rule, input);
	}

	@Override
	public ParseTree getRootNode() {
		CharStream stream = CharStreams.fromString(input);
		MySQLDDLLexer lexer = new MySQLDDLLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MySQLDDLParser parser = new MySQLDDLParser(tokens);
		ParseTree ddlRootNode = parser.execute();
		System.out.println(ddlRootNode.toStringTree(parser));
		return ddlRootNode;
	}

	@Override
	public void parseCreateTable(CreateTableStatement statement, ParseTree ddlRootNode) {
		DDLParserUtils.parseTable(statement, ddlRootNode);
		DDLParserUtils.parseCreateDefinition(statement, ddlRootNode);
	}

	@Override
	public void parseAlterTable(AlterTableStatement statement, ParseTree ddlRootNode) {
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

	@Override
	protected void parseCreateIndex(DDLStatement statement, ParseTree ddlRootNode) {
		DDLParserUtils.parseTable(statement, ddlRootNode);
		DDLParserUtils.parseTableIndexNode(statement, ddlRootNode);
	}

	@Override
	protected void parseAlterIndex(DDLStatement statement, ParseTree ddlRootNode) {
		DDLParserUtils.parseTable(statement, ddlRootNode);
		DDLParserUtils.parseTableIndexNode(statement, ddlRootNode);
	}

	@Override
	protected void parseDropIndex(DDLStatement statement, ParseTree ddlRootNode) {
		DDLParserUtils.parseTable(statement, ddlRootNode);
		DDLParserUtils.parseTableIndexNode(statement, ddlRootNode);		
	}
}
