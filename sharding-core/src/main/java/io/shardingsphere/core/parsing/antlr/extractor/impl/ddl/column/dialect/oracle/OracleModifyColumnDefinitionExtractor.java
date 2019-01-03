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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column.dialect.oracle;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column.ColumnDefinitionExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.ModifyColumnDefinitionSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Modify column definition extractor for Oracle.
 *
 * @author duhongjun
 */
public final class OracleModifyColumnDefinitionExtractor implements CollectionSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionPhraseExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public Collection<ModifyColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> modifyColumnNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.MODIFY_COLUMN);
        if (modifyColumnNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<ModifyColumnDefinitionSegment> result = new LinkedList<>();
        for (ParserRuleContext modifyColumnNode : modifyColumnNodes) {
            for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(modifyColumnNode, RuleName.MODIFY_COL_PROPERTIES)) {
                // it`s not column definition, but can call this method
                Optional<ColumnDefinitionSegment> columnDefinition = columnDefinitionPhraseExtractor.extract(each);
                if (columnDefinition.isPresent()) {
                    result.add(new ModifyColumnDefinitionSegment(null, columnDefinition.get()));
                }
            }
        }
        return result;
    }
}
