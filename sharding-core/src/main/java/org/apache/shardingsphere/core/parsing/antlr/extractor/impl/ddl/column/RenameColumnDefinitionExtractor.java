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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.RenameColumnSegment;

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
