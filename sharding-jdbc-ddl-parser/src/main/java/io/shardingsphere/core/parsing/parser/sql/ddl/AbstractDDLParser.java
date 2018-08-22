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

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.shardingsphere.antlr.utils.DDLParserUtils;
import io.shardingsphere.antlr.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractDDLParser implements SQLParser {
	protected final ShardingRule rule;
	protected final String input;

	public SQLStatement parse() {
		ParseTree ddlRootNode = getDDLRoot();

		SQLStatement statement = createStatement(ddlRootNode);

		return statement;
	}

	private ParseTree getDDLRoot() {
		ParseTree rootNode = getRootNode();
		ParseTree ddlRootNode = null;
		if (rootNode == null) {
			throw new RuntimeException("");
		}

		ddlRootNode = rootNode.getChild(0);
		if (ddlRootNode == null) {
			throw new RuntimeException("");
		}

		return ddlRootNode;
	}

	private DDLStatement createStatement(ParseTree ddlRootNode) {
		DDLStatement statement = null;
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
			statement = new AlterIndexStatement();
			parseAlterIndex(statement, ddlRootNode);
		} else if (DDLType.DROPINDEX.name().equalsIgnoreCase(name)) {
			statement = new DDLStatement();
			parseDropIndex(statement, ddlRootNode);
		} else {
			throw new RuntimeException("unsupported statement");
		}

		return statement;
	}
	
	protected void parseDropTable(DDLStatement statement, ParseTree ddlRootNode) {
		List<ParseTree> tableNameNodes = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "tableName");
		if (null != tableNameNodes) {
			for (ParseTree each : tableNameNodes) {
				DDLParserUtils.parseTableNode(statement, (ParserRuleContext) each);
			}
		}
	}

	protected void parseTruncateTable(DDLStatement statement, ParseTree ddlRootNode) {
		ParseTree secondChild = ddlRootNode.getChild(1);
		int tableNameInd = 1;
		if(secondChild instanceof TerminalNode) {
			tableNameInd = 2;
		}
		
		DDLParserUtils.parseTableNode(statement, (ParserRuleContext)ddlRootNode.getChild(tableNameInd));
	}

	public abstract ParseTree getRootNode();

	protected abstract void parseCreateTable(CreateTableStatement statement, ParseTree ddlRootNode);

	protected abstract void parseAlterTable(AlterTableStatement statement, ParseTree ddlRootNode);

	protected abstract void parseCreateIndex(DDLStatement statement, ParseTree ddlRootNode);
	
	protected abstract void parseAlterIndex(DDLStatement statement, ParseTree ddlRootNode);
	
	protected abstract void parseDropIndex(DDLStatement statement, ParseTree ddlRootNode);
	
}
