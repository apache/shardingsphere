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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.AddColumnDefinitionSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Add column definition extractor.
 *
 * @author duhongjun
 */
public class AddColumnDefinitionExtractor implements CollectionSQLSegmentExtractor {
    
    private final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor();
    
    @Override
    public final Collection<AddColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<AddColumnDefinitionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.ADD_COLUMN)) {
            result.addAll(extractAddColumnDefinitions(each));
        }
        return result;
    }
    
    private Collection<AddColumnDefinitionSegment> extractAddColumnDefinitions(final ParserRuleContext addColumnNode) {
        Collection<AddColumnDefinitionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(addColumnNode, RuleName.COLUMN_DEFINITION)) {
            Optional<ColumnDefinitionSegment> columnDefinitionSegment = columnDefinitionExtractor.extract(each);
            if (columnDefinitionSegment.isPresent()) {
                AddColumnDefinitionSegment addColumnDefinitionSegment = new AddColumnDefinitionSegment(columnDefinitionSegment.get());
                postExtractColumnDefinition(addColumnNode, addColumnDefinitionSegment);
                result.add(addColumnDefinitionSegment);
            }
        }
        return result;
    }
    
    protected void postExtractColumnDefinition(final ParserRuleContext addColumnNode, final AddColumnDefinitionSegment addColumnDefinitionSegment) {
    }
}
