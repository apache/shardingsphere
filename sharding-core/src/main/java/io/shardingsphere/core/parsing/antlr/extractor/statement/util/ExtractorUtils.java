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

package io.shardingsphere.core.parsing.antlr.extractor.statement.util;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Extractor utility.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtractorUtils {
    
    /**
     * Extract column position.
     *
     * @param ancestorNode ancestor node of AST
     * @param columnName column name
     * @return column position object
     */
    public static Optional<ColumnPosition> extractFirstOrAfterColumn(final ParserRuleContext ancestorNode, final String columnName) {
        Optional<ParserRuleContext> firstOrAfterColumnNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.FIRST_OR_AFTER_COLUMN);
        if (!firstOrAfterColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnNameNode = ASTUtils.findFirstChildNode(firstOrAfterColumnNode.get(), RuleName.COLUMN_NAME);
        ColumnPosition result = new ColumnPosition();
        result.setStartIndex(firstOrAfterColumnNode.get().getStart().getStartIndex());
        if (columnNameNode.isPresent()) {
            result.setColumnName(columnName);
            result.setAfterColumn(columnNameNode.get().getText());
        } else {
            result.setFirstColumn(columnName);
        }
        return Optional.of(result);
    }
}
