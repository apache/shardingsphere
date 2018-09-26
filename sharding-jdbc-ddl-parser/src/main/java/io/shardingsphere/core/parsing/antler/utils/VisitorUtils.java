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

package io.shardingsphere.core.parsing.antler.utils;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;

public class VisitorUtils {

    /**
     * Parse table name from root node.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
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
            String literals = null;
            if (pos > 0) {
                literals = name.substring(pos + dotString.length());
            } else {
                pos = 0;
                literals = name;
            }

            statement.getSqlTokens().add(new TableToken(tableNameNode.getStart().getStartIndex(), pos, name));
            statement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), Optional.<String>absent()));
        }
    }

    /**
     * Parse table name node.
     * 
     * @param statement
     *            statement parse result
     * @param tableNameNode
     *            table name node of syntax tree
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
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseColumnDefinition(final CreateTableStatement statement, final ParseTree ddlRootNode) {
        List<ParseTree> columnDefinitions = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "ColumnDefinition");
        if (null == columnDefinitions) {
            return;
        }

        for (final ParseTree each : columnDefinitions) {
            ColumnDefinition column = parseColumnDefinition(each);
            if (null == column) {
                continue;
            }

            statement.getColumnNames().add(column.getName());
            statement.getColumnTypes().add(column.getType());
            if (column.isPrimaryKey()) {
                statement.getPrimaryKeyColumns().add(column.getName());
            }
        }
    }

    /**
     * Parse alter table add column nodes.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseAddColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        List<ParseTree> addColumnCtxs = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "addColumn");
        if (null == addColumnCtxs) {
            return;
        }

        for (ParseTree each : addColumnCtxs) {
            List<ParseTree> columnDefinitionCtxs = TreeUtils.getAllDescendantByRuleName(each, "columnDefinition");
            if (null == columnDefinitionCtxs) {
                continue;
            }

            for (ParseTree columnDefinitionCtx : columnDefinitionCtxs) {
                ColumnDefinition column = parseColumnDefinition(columnDefinitionCtx);
                if (null != column) {
                    statement.getAddColumns().add(column);
                }
            }
        }
    }

    /**
     * Visit drop table modify column nodes.
     * 
     * @param statement
     *            statement parse result
     * @param rootNode
     *            Root node of syntax tree
     */
    public static void visitDropColumn(final AlterTableStatement statement, final ParseTree rootNode) {
        List<ParseTree> dropColumnCtxs = TreeUtils.getAllDescendantByRuleName(rootNode, "dropColumn");
        if (null == dropColumnCtxs) {
            return;
        }

        for (ParseTree each : dropColumnCtxs) {
            List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(each, "columnName");
            if (null == columnNodes) {
                continue;
            }

            for (final ParseTree columnNode : columnNodes) {
                statement.getDropColumns().add(SQLUtil.getExactlyValue(columnNode.getText()));
            }
        }

    }

    /**
     * Parse alter table modify column nodes.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseModifyColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext modifyColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "modifyColumn");
        if (null == modifyColumnCtx) {
            return;
        }

        ColumnDefinition column = parseColumnDefinition(modifyColumnCtx);
        if (null != column) {
            statement.getUpdateColumns().put(column.getName(), column);
        }
    }

    /**
     * Parse drop index.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseDropIndex(final AlterTableStatement statement, final ParseTree ddlRootNode) {

    }

    /**
     * Parse rename index.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
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
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static boolean parseRenameTable(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext renameTableNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "renameTable");
        if (null != renameTableNode) {
            statement.setNewTableName(renameTableNode.getChild(renameTableNode.getChildCount() - 1).getText());
            return true;
        }

        return false;
    }

    /**
     * Parse add primary key.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
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

    /**
     * Parse drop primary key.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseDropPrimaryKey(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext dropPrimaryKeyNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "dropPrimaryKey");
        if (null != dropPrimaryKeyNode) {
            statement.setDropPrimaryKey(true);
        }
    }

    /**
     * Parse drop column.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseDropColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        List<ParseTree> dropColumnNodes = TreeUtils.getAllDescendantByRuleName(ddlRootNode, "dropColumn");
        if (null != dropColumnNodes) {
            for (final ParseTree each : dropColumnNodes) {
                List<ParseTree> columnNameNodes = TreeUtils.getAllDescendantByRuleName(each, "columnName");
                for (ParseTree columnNameNode : columnNameNodes) {
                    if (null != columnNameNode) {
                        statement.getDropColumns().add(columnNameNode.getText());
                    }
                }
            }
        }
    }

    // TODO parse after | before column
    /**
     * Parse alter table single column.
     * 
     * @param statement
     *            statement parse result
     * @param ddlRootNode
     *            DDL root node of syntax tree
     */
    public static void parseSingleColumn(final AlterTableStatement statement, final ParseTree ddlRootNode) {
        ParserRuleContext singleColumnNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ddlRootNode,
                "singleColumn");
        if (null != singleColumnNode) {
            ParserRuleContext columnNameAndDefinitionNode = (ParserRuleContext) TreeUtils
                    .getFirstChildByRuleName(singleColumnNode, "columnDefinition");
            ColumnDefinition column = parseColumnDefinition(columnNameAndDefinitionNode);
            if (null != column) {
                statement.getAddColumns().add(column);
            }
        }
    }

    /**
     * Parse column definition.
     * 
     * @param columnDefinitionNode
     *            column definition rule
     * @return column defition
     */
    public static ColumnDefinition parseColumnDefinition(final ParseTree columnDefinitionNode) {
        if (null == columnDefinitionNode) {
            return null;
        }

        ParserRuleContext columnNameNode = (ParserRuleContext) (ParserRuleContext) TreeUtils
                .getFirstChildByRuleName(columnDefinitionNode, "columnName");

        if (null == columnNameNode) {
            return null;
        }

        ParserRuleContext dataTypeCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(columnDefinitionNode,
                "dataType");

        String typeName = null;
        if (dataTypeCtx != null) {
            typeName = dataTypeCtx.getChild(0).getText();
        }

        Integer length = null;

        ParserRuleContext dataTypeLengthCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(dataTypeCtx,
                "dataTypeLength");

        if (null != dataTypeLengthCtx) {
            if (dataTypeLengthCtx.getChildCount() >= 3) {
                try {
                    length = Integer.parseInt(dataTypeLengthCtx.getChild(1).getText());
                } catch (NumberFormatException e) {
                    // just for checksty
                    length = null;
                }
            }
        }

        ParserRuleContext primaryKeyNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(columnDefinitionNode,
                "primaryKey");
        boolean primaryKey = false;
        if (null != primaryKeyNode) {
            primaryKey = true;
        }

        return new ColumnDefinition(columnNameNode.getText(), typeName, length, primaryKey);
    }
}
