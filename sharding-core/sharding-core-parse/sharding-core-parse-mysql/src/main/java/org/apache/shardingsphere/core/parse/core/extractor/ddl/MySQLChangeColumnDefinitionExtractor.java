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
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.ddl.column.ColumnDefinitionExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.position.ColumnPositionSegment;

import java.util.Map;

/**
 * Change column definition extractor for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLChangeColumnDefinitionExtractor implements OptionalSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public Optional<ModifyColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> changeColumnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.CHANGE_COLUMN_SPECIFICATION);
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
        Optional<ColumnDefinitionSegment> columnDefinitionSegment = columnDefinitionExtractor.extract(columnDefinitionNode.get(), parameterMarkerIndexes);
        if (columnDefinitionSegment.isPresent()) {
            ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(
                    columnDefinitionNode.get().getStart().getStartIndex(), columnDefinitionNode.get().getStop().getStopIndex(), columnDefinitionSegment.get());
            Optional<ColumnPositionSegment> columnPositionSegment = new MySQLColumnPositionExtractor(
                    columnDefinitionSegment.get().getColumnName()).extract(changeColumnNode.get(), parameterMarkerIndexes);
            if (columnPositionSegment.isPresent()) {
                result.setColumnPosition(columnPositionSegment.get());
            }
            return Optional.of(result);
        }
        return Optional.absent();
    }
}
