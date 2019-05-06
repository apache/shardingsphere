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

import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.DropColumnDefinitionSegment;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.HashSet;

/**
 * Drop column definition extractor.
 *
 * @author duhongjun
 */
public final class DropColumnDefinitionExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<DropColumnDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<DropColumnDefinitionSegment> result = new HashSet<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.DROP_COLUMN)) {
            result.addAll(extractDropColumnSegments(each));
        }
        return result;
    }
    
    private Collection<DropColumnDefinitionSegment> extractDropColumnSegments(final ParserRuleContext dropColumnNode) {
        Collection<DropColumnDefinitionSegment> result = new HashSet<>();
        for (ParseTree each : ExtractorUtils.getAllDescendantNodes(dropColumnNode, RuleName.COLUMN_NAME)) {
            result.add(new DropColumnDefinitionSegment(SQLUtil.getExactlyValue(each.getText())));
        }
        return result;
    }
}
