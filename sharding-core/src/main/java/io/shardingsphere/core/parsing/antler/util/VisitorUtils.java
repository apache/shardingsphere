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

package io.shardingsphere.core.parsing.antler.util;

import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnPosition;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor utils.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VisitorUtils {
    
    /**
     * Parse column definition.
     *
     * @param columnDefinitionNode column definition rule
     * @return column definition
     */
    public static ColumnDefinition visitColumnDefinition(final ParserRuleContext columnDefinitionNode) {
        if (null == columnDefinitionNode) {
            return null;
        }
        ParserRuleContext columnNameNode = TreeUtils
                .getFirstChildByRuleName(columnDefinitionNode, RuleNameConstants.COLUMN_NAME);

        if (null == columnNameNode) {
            return null;
        }
        ParserRuleContext dataTypeCtx = TreeUtils.getFirstChildByRuleName(columnDefinitionNode, RuleNameConstants.DATA_TYPE);
        String typeName = null;
        if (null != dataTypeCtx) {
            typeName = dataTypeCtx.getChild(0).getText();
        }
        Integer length = null;
        ParserRuleContext dataTypeLengthCtx = TreeUtils.getFirstChildByRuleName(dataTypeCtx, RuleNameConstants.DATA_TYPE_LENGTH);
        if (null != dataTypeLengthCtx) {
            if (dataTypeLengthCtx.getChildCount() >= 3) {
                try {
                    length = Integer.parseInt(dataTypeLengthCtx.getChild(1).getText());
                } catch (NumberFormatException ignore) {
                }
            }
        }
        ParserRuleContext primaryKeyNode = TreeUtils.getFirstChildByRuleName(columnDefinitionNode, RuleNameConstants.PRIMARY_KEY);
        boolean primaryKey = false;
        if (null != primaryKeyNode) {
            primaryKey = true;
        }
        return new ColumnDefinition(columnNameNode.getText(), typeName, length, primaryKey);
    }
    
    /**
     * Visit column position.
     *
     * @param ancestorNode ancestor node of ast
     * @param columnName column name
     * @return column position object
     */
    public static ColumnPosition visitFirstOrAfter(final ParserRuleContext ancestorNode, final String columnName) {
        ParserRuleContext firstOrAfterColumnContext = TreeUtils.getFirstChildByRuleName(ancestorNode, RuleNameConstants.FIRST_OR_AFTER_COLUMN);
        if (null == firstOrAfterColumnContext) {
            return null;
        }
        ParserRuleContext columnNameCtx = TreeUtils.getFirstChildByRuleName(firstOrAfterColumnContext, "columnName");
        ColumnPosition columnPosition = new ColumnPosition();
        columnPosition.setStartIndex(firstOrAfterColumnContext.getStart().getStartIndex());
        if (null != columnNameCtx) {
            columnPosition.setColumnName(columnName);
            columnPosition.setAfterColumn(columnNameCtx.getText());
        } else {
            columnPosition.setFirstColumn(columnName);
        }
        return columnPosition;
    }
    
    /**
     * Visit indices node.
     *
     * @param ancestorNode ancestor node of ast
     * @param tableName table name
     * @return index token list
     */
    public static List<IndexToken> visitIndices(final ParserRuleContext ancestorNode, final String tableName) {
        List<ParserRuleContext> indexNameCtxs = TreeUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.INDEX_NAME);
        if (null == indexNameCtxs) {
            return null;
        }
        List<IndexToken> result = new ArrayList<>();
        for (ParserRuleContext each : indexNameCtxs) {
            result.add(visitIndex(each, tableName));
        }
        return result;
    }
    
    /**
     * Visit index node.
     *
     * @param indexNameContext index name context
     * @param tableName  table name
     * @return index token
     */
    public static IndexToken visitIndex(final ParserRuleContext indexNameContext, final String tableName) {
        String name = getName(indexNameContext.getText());
        int startPos = indexNameContext.getStop().getStartIndex();
        return new IndexToken(startPos, name, tableName);
    }
    
    /** 
     * Get name from text.
     * 
     * @param text input text
     * @return object name
     */
    public static String getName(final String text) {
        String dotString = Symbol.DOT.getLiterals();
        int pos = text.lastIndexOf(dotString);
        return pos > 0 ? text.substring(pos + dotString.length()) : text;
    }
}
