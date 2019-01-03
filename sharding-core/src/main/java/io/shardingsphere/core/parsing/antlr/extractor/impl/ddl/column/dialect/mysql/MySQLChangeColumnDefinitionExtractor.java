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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column.dialect.mysql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column.ColumnDefinitionExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.ModifyColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Change column definition extractor for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLChangeColumnDefinitionExtractor implements OptionalSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public Optional<ModifyColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> changeColumnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.CHANGE_COLUMN);
        if (!changeColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> oldColumnNameNode = ExtractorUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_NAME);
        if (!oldColumnNameNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnDefinitionNode = ExtractorUtils.findFirstChildNode(changeColumnNode.get(), RuleName.COLUMN_DEFINITION);
        if (!columnDefinitionNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnDefinitionSegment> columnDefinitionSegment = columnDefinitionExtractor.extract(columnDefinitionNode.get());
        if (columnDefinitionSegment.isPresent()) {
            ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(oldColumnNameNode.get().getText(), columnDefinitionSegment.get());
            Optional<ColumnPositionSegment> columnPositionSegment = new MySQLColumnPositionExtractor(columnDefinitionSegment.get().getColumnName()).extract(changeColumnNode.get());
            if (columnPositionSegment.isPresent()) {
                result.setColumnPosition(columnPositionSegment.get());
            }
            return Optional.of(result);
        }
        return Optional.absent();
    }
}
