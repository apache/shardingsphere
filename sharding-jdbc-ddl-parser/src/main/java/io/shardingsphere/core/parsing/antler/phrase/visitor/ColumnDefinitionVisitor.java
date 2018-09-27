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

package io.shardingsphere.core.parsing.antler.phrase.visitor;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

public class ColumnDefinitionVisitor implements PhraseVisitor {

    @Override
    public void visit(ParserRuleContext rootNode, SQLStatement statement) {
        CreateTableStatement createStatement = (CreateTableStatement) statement;

        List<ParseTree> columnDefinitions = TreeUtils.getAllDescendantByRuleName(rootNode, "ColumnDefinition");
        if (null == columnDefinitions) {
            return;
        }

        for (final ParseTree each : columnDefinitions) {
            ColumnDefinition column = parseColumnDefinition(each);
            if (null == column) {
                continue;
            }

            createStatement.getColumnNames().add(column.getName());
            createStatement.getColumnTypes().add(column.getType());
            if (column.isPrimaryKey()) {
                createStatement.getPrimaryKeyColumns().add(column.getName());
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
    protected ColumnDefinition parseColumnDefinition(final ParseTree columnDefinitionNode) {
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
