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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ColumnDefinitionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.phrase.ColumnDefinitionPhraseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;

/**
 * Column definition extract handler.
 *
 * @author duhongjun
 */
public final class ColumnDefinitionExtractHandler implements ASTExtractHandler {
    
    private final ColumnDefinitionPhraseExtractor columnDefinitionPhraseExtractor = new ColumnDefinitionPhraseExtractor();
    
    @Override
    public Optional<ExtractResult> extract(final ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> columnDefinitionNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.COLUMN_DEFINITION);
        if (columnDefinitionNodes.isEmpty()) {
            return Optional.absent();
        }
        ColumnDefinitionExtractResult result = new ColumnDefinitionExtractResult();
        for (ParserRuleContext each : columnDefinitionNodes) {
            Optional<ColumnDefinition> columnDefinition = columnDefinitionPhraseExtractor.extract(each);
            if (columnDefinition.isPresent()) {
                columnDefinition.get().setAdd(true);
                result.getColumnDefinitions().add(columnDefinition.get());
            }
        }
        return Optional.<ExtractResult>of(result);
    }
}
