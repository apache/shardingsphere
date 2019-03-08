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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dml;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.TableNamesExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql.AbstractFromWhereExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.dml.DeleteFromWhereSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;

import java.util.Map;

/**
 * Delete from extractor.
 *
 * @author duhongjun
 */
public final class DeleteFromWhereExtractor extends AbstractFromWhereExtractor {
    
    protected FromWhereSegment createSegment() {
        return new DeleteFromWhereSegment();
    }
    
    @Override
    protected Optional<ParserRuleContext> extractTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> placeholderIndexes) {
        for (TableSegment each : new TableNamesExtractor().extract(ancestorNode)) {
            fillTableResult(fromWhereSegment, each);
        }
        if (fromWhereSegment.getTableAliases().isEmpty()) {
            return Optional.absent();
        }
        if (1 < fromWhereSegment.getTableAliases().size()) {
            throw new SQLParsingUnsupportedException("Cannot support Multiple-Table.");
        }
        return ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
    }
}
