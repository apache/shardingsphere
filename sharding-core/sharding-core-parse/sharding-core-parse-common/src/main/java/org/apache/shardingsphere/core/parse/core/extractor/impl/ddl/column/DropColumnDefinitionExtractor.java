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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.core.parse.core.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Drop column definition extractor.
 *
 * @author duhongjun
 */
public final class DropColumnDefinitionExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<DropColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Collection<DropColumnDefinitionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.DROP_COLUMN_SPECIFICATION)) {
            result.addAll(extractDropColumnSegments(each));
        }
        return result;
    }
    
    private Collection<DropColumnDefinitionSegment> extractDropColumnSegments(final ParserRuleContext dropColumnNode) {
        Collection<DropColumnDefinitionSegment> result = new LinkedList<>();
        for (ParseTree each : ExtractorUtils.getAllDescendantNodes(dropColumnNode, RuleName.COLUMN_NAME)) {
            result.add(new DropColumnDefinitionSegment(dropColumnNode.getStart().getStartIndex(), dropColumnNode.getStop().getStartIndex(), SQLUtil.getExactlyValue(each.getText())));
        }
        return result;
    }
}
