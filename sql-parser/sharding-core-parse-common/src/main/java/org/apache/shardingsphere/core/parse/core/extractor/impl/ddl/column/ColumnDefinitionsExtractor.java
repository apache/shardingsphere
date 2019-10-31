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

package org.apache.shardingsphere.core.parse.core.extractor.impl.ddl.column;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Column definitions extractor.
 *
 * @author duhongjun
 */
public final class ColumnDefinitionsExtractor implements CollectionSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public Collection<ColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Collection<ColumnDefinitionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.COLUMN_DEFINITION)) {
            Optional<ColumnDefinitionSegment> columnDefinitionSegment = columnDefinitionExtractor.extract(each, parameterMarkerIndexes);
            if (columnDefinitionSegment.isPresent()) {
                result.add(columnDefinitionSegment.get());
            }
        }
        return result;
    }
}
