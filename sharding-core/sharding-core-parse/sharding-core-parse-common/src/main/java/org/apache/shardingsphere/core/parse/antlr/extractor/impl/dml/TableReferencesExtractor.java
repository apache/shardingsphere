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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.common.table.TablesExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Table references extractor.
 *
 * @author zhangliang
 */
public final class TableReferencesExtractor implements CollectionSQLSegmentExtractor {
    
    private final TablesExtractor tablesExtractor = new TablesExtractor();
    
    private final ColumnExtractor columnExtractor = new ColumnExtractor();
    
    @Override
    public Collection<TableSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> tableReferencesNodes = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCES);
        if (!tableReferencesNodes.isPresent()) {
            return Collections.emptyList();
        }
        Collection<TableSegment> result = new LinkedList<>();
        Collection<TableSegment> tableSegments = tablesExtractor.extract(tableReferencesNodes.get(), parameterMarkerIndexes);
        result.addAll(tableSegments);
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(tableReferencesNodes.get(), RuleName.COLUMN_NAME)) {
            Optional<ColumnSegment> columnSegment = columnExtractor.extract(each, parameterMarkerIndexes);
            if (columnSegment.isPresent() && columnSegment.get().getOwner().isPresent() && isTableName(columnSegment.get().getOwner().get(), tableSegments)) {
                result.add(new TableSegment(columnSegment.get().getStartIndex(), columnSegment.get().getOwner().get(), columnSegment.get().getOwnerQuoteCharacter()));
            }
        }
        return result;
    }
    
    private boolean isTableName(final String columnOwner, final Collection<TableSegment> tableSegments) {
        for (TableSegment each : tableSegments) {
            if (each.getName().equalsIgnoreCase(columnOwner)) {
                return true;
            }
        }
        return false;
    }
}
