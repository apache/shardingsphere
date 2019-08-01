/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.core.extractor.ddl;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.position.ColumnPositionSegment;

import java.util.Map;

/**
 * Column position extractor for MySQL.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class MySQLColumnPositionExtractor implements OptionalSQLSegmentExtractor {
    
    private final String columnName;
    
    @Override
    public Optional<ColumnPositionSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> firstOrAfterColumnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.FIRST_OR_AFTER_COLUMN);
        if (!firstOrAfterColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnNameNode = ExtractorUtils.findFirstChildNode(firstOrAfterColumnNode.get(), RuleName.COLUMN_NAME);
        int startIndex = firstOrAfterColumnNode.get().getStart().getStartIndex();
        int stopIndex = firstOrAfterColumnNode.get().getStop().getStopIndex();
        ColumnPositionSegment result = columnNameNode.isPresent()
                ? new ColumnAfterPositionSegment(startIndex, stopIndex, columnName, columnNameNode.get().getText()) : new ColumnFirstPositionSegment(startIndex, stopIndex, columnName);
        return Optional.of(result);
    }
}
