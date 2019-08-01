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
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;

import java.util.Map;

/**
 * Column definition extractor.
 * 
 * @author duhongjun
 * @author zhangliang
 */
public final class ColumnDefinitionExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> columnNameNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_NAME);
        if (!columnNameNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> dataTypeNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.DATA_TYPE);
        Optional<String> dataTypeText = dataTypeNode.isPresent() ? Optional.of(dataTypeNode.get().getChild(0).getChild(0).getText()) : Optional.<String>absent();
        boolean isPrimaryKey = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.PRIMARY_KEY).isPresent();
        return Optional.of(new ColumnDefinitionSegment(
                columnNameNode.get().getStart().getStartIndex(), columnNameNode.get().getStop().getStopIndex(), columnNameNode.get().getText(), dataTypeText.orNull(), isPrimaryKey));
    }
}
