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

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine.dialect.mysql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.ColumnDefinitionExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnPositionSegment;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Change column extractor for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLChangeColumnExtractor implements OptionalSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionPhraseExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public Optional<ColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> changeColumnNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.CHANGE_COLUMN);
        if (!changeColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> oldColumnNode = ASTUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_NAME);
        if (!oldColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnDefinitionNode = ASTUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_DEFINITION);
        if (!columnDefinitionNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnDefinitionSegment> result = columnDefinitionPhraseExtractor.extract(columnDefinitionNode.get());
        if (result.isPresent()) {
            result.get().setOldName(oldColumnNode.get().getText());
            Optional<ColumnPositionSegment> columnPosition = new MySQLColumnPositionExtractor(result.get().getName()).extract(changeColumnNode.get());
            if (columnPosition.isPresent()) {
                result.get().setPosition(columnPosition.get());
            }
        }
        return result;
    }
}
