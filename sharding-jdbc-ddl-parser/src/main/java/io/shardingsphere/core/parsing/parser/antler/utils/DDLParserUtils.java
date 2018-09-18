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

package io.shardingsphere.core.parsing.parser.antler.utils;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.parser.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;
import io.shardingsphere.parser.antlr.mysql.MySQLDDLParser;

public class DDLParserUtils {

    /**
     * Parse table name from root node.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseTable(final SQLStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext tableNameNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "tableName");
        if (null != tableNameNode) {
            String name = tableNameNode.getText();
            if (null == name) {
                return;
            }

            String dotString = Symbol.DOT.getLiterals();
            int pos = name.lastIndexOf(dotString);
            String literals = name;
            if (pos > 0) {
                literals = name.substring(dotString.length() + 1);
            } else {
                pos = 0;
            }

            statement.getSqlTokens().add(new TableToken(tableNameNode.getStart().getStartIndex(), pos, name));
            statement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), Optional.<String>absent()));
        }
    }

    /**
     * Parse table name node.
     * 
     * @param statement statement parse result
     * @param tableNameNode table name node of syntax tree
     */
    public static void parseTableNode(final SQLStatement statement, final ParserRuleContext tableNameNode) {
        if (null == tableNameNode) {
            return;
        }

        String name = tableNameNode.getText();
        if (null == name) {
            return;
        }

        String dotString = Symbol.DOT.getLiterals();
        int pos = name.lastIndexOf(dotString);
        String literals = name;
        if (pos > 0) {
            literals = name.substring(dotString.length() + 1);
        } else {
            pos = 0;
        }

        statement.getSqlTokens().add(new TableToken(tableNameNode.getStart().getStartIndex(), pos, name));
        statement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), Optional.<String>absent()));
    }

    /**
     * Parse create table column definition node.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseCreateDefinition(final CreateTableStatement statement, final ParseTree ddlRootNode) {
        List<ParseTree> createDefinitionNodes = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "createDefinition");
        if (null == createDefinitionNodes) {
            return;
        }

        for (final ParseTree each : createDefinitionNodes) {
            if (each.getClass().getSimpleName().startsWith("ColumnNameAndDefinition")) {
                ColumnDefinition column = parseColumnDefinition(each);
                if (null == column) {
                    continue;
                }

                statement.getColumnNames().add(column.getName());
                statement.getColumnTypes().add(column.getType());
                if (column.isPrimaryKey()) {
                    statement.getPrimaryKeyColumns().add(column.getName());
                }
            } else if (each.getClass().getSimpleName().startsWith("constraintDefinition")) {
                // TODO add primary key
            } else if (each.getClass().getSimpleName().startsWith("indexDefinition")) {
                // TODO add index
            }
        }
    }

    /**
     * Parse alter table add column nodes.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseAddColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        parseSingleColumn(statement, ddlRootNode);

        ParserRuleContext multiColumnNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "multiColumn");
        if (null == multiColumnNode) {
            return;
        }

        List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(multiColumnNode, "columnNameAndDefinition");
        if (null == columnNodes) {
            return;
        }

        for (final ParseTree each : columnNodes) {
            ColumnDefinition column = parseColumnDefinition(each);
            if (null != column) {
                statement.getAddColumns().add(column);
            }
        }
    }

    /**
     * Parse alter table modify column nodes.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseModifyColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext modifyColumnNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "modifyColumn");
        if (null == modifyColumnNode) {
            return;
        }

        ParseTree secondChild = modifyColumnNode.getChild(1);
        int start = 1;
        if (secondChild instanceof TerminalNode) {
            start = 2;
        }

        ColumnDefinition column = parseColumnDefinition(modifyColumnNode.getChild(start));

        if (null != column) {
            statement.getUpdateColumns().put(column.getName(), column);
        }
    }

    /**
     * Parse add index nodes.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseAddIndex(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext indexDefOptionNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "indexDefOption");
        if (null != indexDefOptionNode) {
            parseTableIndexNode(statement, indexDefOptionNode);
        }
    }

    /**
     * Parse index node.
     * 
     * @param statement statement parse result
     * @param ancestorNode ancestor of index node
     * @return indexName node
     */
    public static ParserRuleContext parseTableIndexNode(final SQLStatement statement, final ParseTree ancestorNode) {
        if (null != ancestorNode) {
            ParserRuleContext indexNameNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode,
                    "indexName");
            if (null != indexNameNode) {
                statement.getSqlTokens().add(new IndexToken(indexNameNode.getStart().getStartIndex(),
                        indexNameNode.getText(), statement.getTables().getSingleTableName()));
                return indexNameNode;
            }
        }

        return null;
    }

    /**
     * Parse drop index.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseDropIndex(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext dropIndexDefNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "dropIndexDef");
        if (null == dropIndexDefNode) {
            return;
        }

        ParserRuleContext indexNameNode = (ParserRuleContext) dropIndexDefNode
                .getChild(dropIndexDefNode.getChildCount() - 1);
        if (null != indexNameNode) {
            statement.getSqlTokens().add(new IndexToken(indexNameNode.getStart().getStartIndex(),
                    indexNameNode.getText(), statement.getTables().getSingleTableName()));
        }
    }

    /**
     * Parse rename index.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseRenameIndex(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext renameIndexNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "renameIndex");
        if (null != renameIndexNode) {
            ParserRuleContext oldIndexNode = (ParserRuleContext) renameIndexNode.getChild(2);
            ParserRuleContext newIndexNode = (ParserRuleContext) renameIndexNode
                    .getChild(renameIndexNode.getChildCount() - 1);
            statement.getSqlTokens().add(new IndexToken(oldIndexNode.getStart().getStartIndex(), oldIndexNode.getText(),
                    statement.getTables().getSingleTableName()));
            statement.getSqlTokens().add(new IndexToken(newIndexNode.getStart().getStartIndex(), newIndexNode.getText(),
                    statement.getTables().getSingleTableName()));
        }
    }

    /**
     * Parse rename table.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseRenameTable(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext renameTableNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "renameTable");
        if (null != renameTableNode) {
            statement.setNewTableName(renameTableNode.getChild(renameTableNode.getChildCount() - 1).getText());
        }
    }

    /**
     * Parse add primary key.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseAddPrimaryKey(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext constraintDefinitionNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "constraintDefinition");
        if (null == constraintDefinitionNode) {
            return;
        }

        ParserRuleContext primaryKeyOptionNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "primaryKeyOption");
        if (null == primaryKeyOptionNode) {
            return;
        }

        List<ParseTree> keyPartNodes = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "keyPart");
        if (null == keyPartNodes) {
            return;
        }

        for (final ParseTree each : keyPartNodes) {
            String columnName = each.getChild(0).getText();
            ColumnDefinition updateColumn = statement.getUpdateColumns().get(columnName);
            if (null == updateColumn) {
                updateColumn = new ColumnDefinition(columnName, null, null, true);
                statement.getUpdateColumns().put(columnName, updateColumn);
            } else {
                updateColumn.setPrimaryKey(true);
            }
        }
    }

    /**Parse drop primary key.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseDropPrimaryKey(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext dropPrimaryKeyNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "dropPrimaryKey");
        if (null != dropPrimaryKeyNode) {
            statement.setDropPrimaryKey(true);
        }
    }

    /** Parse drop column.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseDropColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        List<ParseTree> dropColumnNodes = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "dropColumn");
        if (null != dropColumnNodes) {
            for (final ParseTree each : dropColumnNodes) {
                String columnName = each.getChild(each.getChildCount() - 1).getText();
                if (null != columnName) {
                    statement.getDropColumns().add(columnName);
                }
            }
        }
    }

    // TODO parse after | before column
    /**Parse alter table single column.
     * 
     * @param statement statement parse result
     * @param ddlRootNode DDL root node of syntax tree
     */
    public static void parseSingleColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext singleColumnNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "singleColumn");
        if (null != singleColumnNode) {
            ParserRuleContext columnNameAndDefinitionNode = (ParserRuleContext) TreeUtils
                    .getFirstChildByRuleName(singleColumnNode, "columnNameAndDefinition");
            ColumnDefinition column = parseColumnDefinition(columnNameAndDefinitionNode);
            if (null != column) {
                statement.getAddColumns().add(column);
            }
        }
    }

    /**Parse column definition.
     * 
     * @param columnNameAndDefinitionNode column definition rule
     * @return column defition
     */
    public static ColumnDefinition parseColumnDefinition(final ParseTree columnNameAndDefinitionNode) {
        if (null == columnNameAndDefinitionNode) {
            return null;
        }

        ParserRuleContext columnNameNode = (ParserRuleContext) columnNameAndDefinitionNode.getChild(0);
        ParserRuleContext columnDefinitionNode = (ParserRuleContext) columnNameAndDefinitionNode.getChild(1);
        ParserRuleContext dataTypeRule = (ParserRuleContext) columnDefinitionNode.getChild(0);
        TerminalNode dateType = (TerminalNode) dataTypeRule.getChild(0);

        Integer length = null;
        if (dataTypeRule.getChildCount() > 1) {
            TerminalNode lengthNode = TreeUtils.getFirstTerminalByType(dataTypeRule.getChild(1), MySQLDDLParser.NUMBER);
            if (null != lengthNode) {
                try {
                    length = Integer.parseInt(lengthNode.getText());
                } catch (NumberFormatException e) {
                }
            }
        }

        TerminalNode primaryKeyNode = TreeUtils.getFirstTerminalByType(columnDefinitionNode, MySQLDDLParser.PRIMARY);
        boolean primaryKey = false;
        if (null != primaryKeyNode) {
            primaryKey = true;
        }

        return new ColumnDefinition(columnNameNode.getText(), dateType.getText(), length, primaryKey);
    }
}
