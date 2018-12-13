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

package io.shardingsphere.core.parsing.antlr.extractor.impl.dialect.mysql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.IndexNameExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.IndexSegment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Drop index extractor for MySQL.
 *
 * @author duhongjun
 */
public final class MySQLDropIndexExtractor implements CollectionSQLSegmentExtractor {
    
    private final IndexNameExtractor indexNameExtractor = new IndexNameExtractor();
    
    @Override
    public Collection<IndexSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> dropIndexNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.DROP_INDEX_REF);
        if (dropIndexNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<IndexSegment> result = new LinkedList<>();
        for (ParserRuleContext each : dropIndexNodes) {
            int childCnt = each.getChildCount();
            if (0 == childCnt) {
                continue;
            }
            ParseTree lastChild = each.getChild(childCnt - 1);
            if (!(lastChild instanceof ParserRuleContext)) {
                continue;
            }
            Optional<IndexSegment> extractResult = indexNameExtractor.extract((ParserRuleContext) lastChild);
            if (extractResult.isPresent()) {
                result.add(extractResult.get());
            }
        }
        return result;
    }
}
