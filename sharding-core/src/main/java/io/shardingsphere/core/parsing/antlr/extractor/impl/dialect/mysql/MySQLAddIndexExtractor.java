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

import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.IndexNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.IndexSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;

/**
 * Add index extractor for MySQL.
 *
 * @author duhongjun
 */
public final class MySQLAddIndexExtractor implements CollectionSQLSegmentExtractor {
    
    private final IndexNamesExtractor indexesNameExtractor = new IndexNamesExtractor();
    
    @Override
    public Collection<IndexSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> addIndexNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.ADD_INDEX);
        if (addIndexNodes.isEmpty()) {
            return Collections.emptyList();
        }
       
        return indexesNameExtractor.extract(ancestorNode);
    }
}
