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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.dialect.mysql;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ColumnDefinitionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.phrase.ColumnDefinitionPhraseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;

/**
 * Change column extract handler for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLChangeColumnExtractHandler implements ASTExtractHandler {
    
    private final ColumnDefinitionPhraseExtractor columnDefinitionPhraseExtractor = new ColumnDefinitionPhraseExtractor();
    
    @Override
    public ExtractResult extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> changeColumnNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.CHANGE_COLUMN);
        if (!changeColumnNode.isPresent()) {
            return null;
        }
        Optional<ParserRuleContext> oldColumnNode = ASTUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_NAME);
        if (!oldColumnNode.isPresent()) {
            return null;
        }
        Optional<ParserRuleContext> columnDefinitionNode = ASTUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_DEFINITION);
        if (!columnDefinitionNode.isPresent()) {
            return null;
        }
        Optional<ColumnDefinition> columnDefinition = columnDefinitionPhraseExtractor.extract(columnDefinitionNode.get());
        if (!columnDefinition.isPresent()) {
            return null;
        }
        columnDefinition.get().setOldName(oldColumnNode.get().getText());
        ColumnDefinitionExtractResult extractResult = new ColumnDefinitionExtractResult();
        extractResult.getColumnDefintions().add(columnDefinition.get());
        Optional<ColumnPosition> columnPosition = ExtractorUtils.extractFirstOrAfterColumn(changeColumnNode.get(), columnDefinition.get().getName());
        if (columnPosition.isPresent()) {
            columnDefinition.get().setPosition(columnPosition.get());
        }
        return extractResult;
    }
}
