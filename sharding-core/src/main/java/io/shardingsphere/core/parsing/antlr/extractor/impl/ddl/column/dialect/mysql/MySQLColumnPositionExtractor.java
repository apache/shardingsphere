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
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnAfterPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnFirstPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Column position extractor for MySQL.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class MySQLColumnPositionExtractor implements OptionalSQLSegmentExtractor {
    
    private final String columnName;
    
    @Override
    public Optional<ColumnPositionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> firstOrAfterColumnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.FIRST_OR_AFTER_COLUMN);
        if (!firstOrAfterColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnNameNode = ExtractorUtils.findFirstChildNode(firstOrAfterColumnNode.get(), RuleName.COLUMN_NAME);
        ColumnPositionSegment result = columnNameNode.isPresent()
                ? new ColumnAfterPositionSegment(columnName, firstOrAfterColumnNode.get().getStart().getStartIndex(), columnNameNode.get().getText())
                : new ColumnFirstPositionSegment(columnName, firstOrAfterColumnNode.get().getStart().getStartIndex());
        return Optional.of(result);
    }
}
