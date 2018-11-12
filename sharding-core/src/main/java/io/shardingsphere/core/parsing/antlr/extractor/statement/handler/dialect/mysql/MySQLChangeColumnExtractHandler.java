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

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.phrase.ColumnDefinitionPhraseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Change column extract handler for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLChangeColumnExtractHandler implements ASTExtractHandler {
    
    private final ColumnDefinitionPhraseExtractor columnDefinitionPhraseExtractor = new ColumnDefinitionPhraseExtractor();
    
    @Override
    public void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        Optional<ParserRuleContext> changeColumnNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.CHANGE_COLUMN);
        if (!changeColumnNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> oldColumnNode = ASTUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_NAME);
        if (!oldColumnNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> columnDefinitionNode = ASTUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_DEFINITION);
        if (!columnDefinitionNode.isPresent()) {
            return;
        }
        MySQLAlterTableStatement alterStatement = (MySQLAlterTableStatement) statement;
        Optional<ColumnDefinition> columnDefinition = columnDefinitionPhraseExtractor.extract(columnDefinitionNode.get());
        if (!columnDefinition.isPresent()) {
            return;
        }
        alterStatement.getUpdateColumns().put(oldColumnNode.get().getText(), columnDefinition.get());
        Optional<ColumnPosition> columnPosition = ExtractorUtils.extractFirstOrAfterColumn(changeColumnNode.get(), columnDefinition.get().getName());
        if (columnPosition.isPresent()) {
            alterStatement.getPositionChangedColumns().add(columnPosition.get());
        }
    }
}
