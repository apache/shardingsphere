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
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.RenameColumnSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Iterator;

/**
 * Rename column definition extractor.
 * 
 * @author duhongjun
 */
public final class RenameColumnDefinitionExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<RenameColumnSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> modifyColumnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.RENAME_COLUMN);
        if (!modifyColumnNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> columnNodes = ExtractorUtils.getAllDescendantNodes(modifyColumnNode.get(), RuleName.COLUMN_NAME);
        if (2 != columnNodes.size()) {
            return Optional.absent();
        }
        Iterator<ParserRuleContext> iterator = columnNodes.iterator();
        return Optional.of(new RenameColumnSegment(iterator.next().getText(), iterator.next().getText()));
    }
}
