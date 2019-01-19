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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.util.SQLUtil;

/**
 * Column extract extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class ColumnSegmentExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ColumnSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> columnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_NAME);
        if (!columnNode.isPresent()) {
            return Optional.absent();
        }
        String columnText = columnNode.get().getText();
        int dotStartIndex = columnText.lastIndexOf(Symbol.DOT.getLiterals());
        String columnName;
        Optional<String> ownerName;
        if (-1 != dotStartIndex) {
            columnName = columnText.substring(dotStartIndex + 1);
            ownerName = Optional.of(SQLUtil.getExactlyValue(columnText.substring(0, dotStartIndex)));
        } else {
            columnName = columnText;
            ownerName = Optional.absent();
        }
        columnName = SQLUtil.getExactlyValue(columnName);
        ColumnSegment result = new ColumnSegment(columnName, columnNode.get().getStart().getStartIndex());
        result.setOwner(ownerName.orNull());
        return Optional.of(result);
    }
}
