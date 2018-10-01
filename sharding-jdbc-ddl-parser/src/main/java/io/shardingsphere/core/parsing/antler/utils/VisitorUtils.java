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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnPosition;

public class VisitorUtils {
    /**
     * Parse column definition.
     * 
     * @param columnDefinitionNode
     *            column definition rule
     * @return column defition
     */
    public static ColumnDefinition visitColumnDefinition(final ParseTree columnDefinitionNode) {
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
    
    public static ColumnPosition visitFirstOrAfter(ParserRuleContext ancestorNode, String columnName) {
        ParserRuleContext firstOrAfterColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode,
                "firstOrAfterColumn");
        if (null == firstOrAfterColumnCtx) {
            return null;
        }

        ParseTree columnNameCtx = TreeUtils.getFirstChildByRuleName(firstOrAfterColumnCtx, "columnName");
        ColumnPosition columnPosition = new ColumnPosition();
        columnPosition.setStartIndex(firstOrAfterColumnCtx.getStart().getStartIndex());
        
        if (columnNameCtx != null) {
            columnPosition.setColumnName(columnName);
            columnPosition.setAfterColumn(columnNameCtx.getText());
        }else {
            columnPosition.setFirstColumn(columnName);
        }

        return columnPosition;
    }
}
