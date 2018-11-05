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

import com.google.common.base.Optional;
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
 * Visitor utility.
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
    public static Optional<ColumnDefinition> visitColumnDefinition(final ParserRuleContext columnDefinitionNode) {
        if (null == columnDefinitionNode) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnNameNode = ASTUtils.findFirstChildByRuleName(columnDefinitionNode, RuleNameConstants.COLUMN_NAME);
        if (!columnNameNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> dataTypeContext = ASTUtils.findFirstChildByRuleName(columnDefinitionNode, RuleNameConstants.DATA_TYPE);
        String typeName = null;
        if (dataTypeContext.isPresent()) {
            typeName = dataTypeContext.get().getChild(0).getText();
        }
        Integer length = null;
        Optional<ParserRuleContext> dataTypeLengthContext = ASTUtils.findFirstChildByRuleName(dataTypeContext.get(), RuleNameConstants.DATA_TYPE_LENGTH);
        if (dataTypeLengthContext.isPresent()) {
            if (dataTypeLengthContext.get().getChildCount() >= 3) {
                try {
                    length = Integer.parseInt(dataTypeLengthContext.get().getChild(1).getText());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildByRuleName(columnDefinitionNode, RuleNameConstants.PRIMARY_KEY);
        boolean primaryKey = false;
        if (primaryKeyNode.isPresent()) {
            primaryKey = true;
        }
        return Optional.of(new ColumnDefinition(columnNameNode.get().getText(), typeName, length, primaryKey));
    }
    
    /**
     * Visit column position.
     *
     * @param ancestorNode ancestor node of ast
     * @param columnName column name
     * @return column position object
     */
    public static ColumnPosition visitFirstOrAfter(final ParserRuleContext ancestorNode, final String columnName) {
        Optional<ParserRuleContext> firstOrAfterColumnContext = ASTUtils.findFirstChildByRuleName(ancestorNode, RuleNameConstants.FIRST_OR_AFTER_COLUMN);
        if (!firstOrAfterColumnContext.isPresent()) {
            return null;
        }
        Optional<ParserRuleContext> columnNameContext = ASTUtils.findFirstChildByRuleName(firstOrAfterColumnContext.get(), "columnName");
        ColumnPosition result = new ColumnPosition();
        result.setStartIndex(firstOrAfterColumnContext.get().getStart().getStartIndex());
        if (columnNameContext.isPresent()) {
            result.setColumnName(columnName);
            result.setAfterColumn(columnNameContext.get().getText());
        } else {
            result.setFirstColumn(columnName);
        }
        return result;
    }
    
    /**
     * Visit indices node.
     *
     * @param ancestorNode ancestor node of ast
     * @param tableName table name
     * @return index token list
     */
    public static List<IndexToken> visitIndices(final ParserRuleContext ancestorNode, final String tableName) {
        List<IndexToken> result = new ArrayList<>();
        for (ParserRuleContext each : ASTUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.INDEX_NAME)) {
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
        int startPosition = indexNameContext.getStop().getStartIndex();
        return new IndexToken(startPosition, name, tableName);
    }
    
    /** 
     * Get name from text.
     * 
     * @param text input text
     * @return object name
     */
    public static String getName(final String text) {
        String dotString = Symbol.DOT.getLiterals();
        int position = text.lastIndexOf(dotString);
        return position > 0 ? text.substring(position + dotString.length()) : text;
    }
}
