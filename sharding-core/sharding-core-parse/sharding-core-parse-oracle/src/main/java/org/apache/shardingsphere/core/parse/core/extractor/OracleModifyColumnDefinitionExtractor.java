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

package org.apache.shardingsphere.core.parse.core.extractor;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.ddl.column.ColumnDefinitionExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Modify column definition extractor for Oracle.
 *
 * @author duhongjun
 */
public final class OracleModifyColumnDefinitionExtractor implements CollectionSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionPhraseExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public Collection<ModifyColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Collection<ParserRuleContext> modifyColumnNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.MODIFY_COLUMN_SPECIFICATION);
        if (modifyColumnNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<ModifyColumnDefinitionSegment> result = new LinkedList<>();
        for (ParserRuleContext modifyColumnNode : modifyColumnNodes) {
            for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(modifyColumnNode, RuleName.MODIFY_COL_PROPERTIES)) {
                // it`s not column definition, but can call this method
                Optional<ColumnDefinitionSegment> columnDefinition = columnDefinitionPhraseExtractor.extract(each, parameterMarkerIndexes);
                if (columnDefinition.isPresent()) {
                    result.add(new ModifyColumnDefinitionSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), columnDefinition.get()));
                }
            }
        }
        return result;
    }
}
