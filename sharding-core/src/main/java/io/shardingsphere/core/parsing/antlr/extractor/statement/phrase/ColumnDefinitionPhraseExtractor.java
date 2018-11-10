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

package io.shardingsphere.core.parsing.antlr.extractor.statement.phrase;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Column definition phrase extractor.
 * 
 * @author duhongjun
 * @author zhangliang
 */
public final class ColumnDefinitionPhraseExtractor implements PhraseExtractor<ColumnDefinition> {
    
    @Override
    public Optional<ColumnDefinition> extract(final ParserRuleContext columnDefinitionNode) {
        Optional<ParserRuleContext> columnNameNode = ASTUtils.findFirstChildNode(columnDefinitionNode, RuleName.COLUMN_NAME);
        if (!columnNameNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> dataTypeNode = ASTUtils.findFirstChildNode(columnDefinitionNode, RuleName.DATA_TYPE);
        Optional<String> dataTypeText = dataTypeNode.isPresent() ? Optional.of(dataTypeNode.get().getChild(0).getText()) : Optional.<String>absent();
        Optional<Integer> dataTypeLength = dataTypeNode.isPresent() ? getDataTypeLength(dataTypeNode.get()) : Optional.<Integer>absent();
        boolean isPrimaryKey = ASTUtils.findFirstChildNode(columnDefinitionNode, RuleName.PRIMARY_KEY).isPresent();
        return Optional.of(new ColumnDefinition(columnNameNode.get().getText(), dataTypeText.orNull(), dataTypeLength.orNull(), isPrimaryKey));
    }
    
    private Optional<Integer> getDataTypeLength(final ParserRuleContext dataTypeContext) {
        Optional<ParserRuleContext> dataTypeLengthNode = ASTUtils.findFirstChildNode(dataTypeContext, RuleName.DATA_TYPE_LENGTH);
        if (!dataTypeLengthNode.isPresent() || dataTypeLengthNode.get().getChildCount() < 3) {
            return Optional.absent();
        }
        try {
            return Optional.of(Integer.parseInt(dataTypeLengthNode.get().getChild(1).getText()));
        } catch (final NumberFormatException ignored) {
            return Optional.absent();
        }
    }
}
