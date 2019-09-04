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

package org.apache.shardingsphere.core.parse.core.extractor.impl.common.column;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;

import java.util.Map;

/**
 * Column extractor.
 *
 * @author duhongjun
 * @author zhangliang
 */
public final class ColumnExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ColumnSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> columnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_NAME);
        return columnNode.isPresent() ? Optional.of(getColumnSegment(columnNode.get())) : Optional.<ColumnSegment>absent();
    }
    
    private ColumnSegment getColumnSegment(final ParserRuleContext columnNode) {
        ParserRuleContext nameNode = ExtractorUtils.getFirstChildNode(columnNode, RuleName.NAME);
        ColumnSegment result = new ColumnSegment(columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex(), nameNode.getText());
        Optional<ParserRuleContext> ownerNode = ExtractorUtils.findFirstChildNodeNoneRecursive(columnNode, RuleName.OWNER);
        if (ownerNode.isPresent()) {
            result.setOwner(new TableSegment(ownerNode.get().getStart().getStartIndex(), ownerNode.get().getStop().getStopIndex(), ownerNode.get().getText()));
        }
        return result;
    }
}
