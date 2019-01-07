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

package io.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.util.SQLUtil;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;

/**
 * Column extract extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class ColumnSegmentExtractor implements OptionalSQLSegmentExtractor {
    
    private final Map<String, String> tableAlias;
    
    @Override
    public Optional<ColumnSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> columnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_NAME);
        if (!columnNode.isPresent()) {
            return Optional.absent();
        }
        String columnText = columnNode.get().getText();
        int dotPosition = columnText.contains(Symbol.DOT.getLiterals()) ? columnText.lastIndexOf(Symbol.DOT.getLiterals()) : 0;
        String columnName = columnText;
        Optional<String> ownerName;
        String tableName = "";
        if (0 < dotPosition) {
            columnName = columnText.substring(dotPosition + 1);
            ownerName = Optional.of(SQLUtil.getExactlyValue(columnText.substring(0, dotPosition)));
            tableName = tableAlias.get(ownerName.get());
        } else {
            ownerName = Optional.absent();
        }
        if ("".equals(tableName) && 1 == tableAlias.size()) {
            tableName = tableAlias.values().iterator().next();
        }
        columnName = SQLUtil.getExactlyValue(columnName);
        return Optional.of(new ColumnSegment(ownerName, columnName, tableName, columnNode.get().getStart().getStartIndex()));
    }
}
