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

package io.shardingsphere.core.parsing.antlr.extractor.impl.dialect.sqlserver;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.IndexNameExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.IndexSegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Add index extractor for SQLServer.
 * 
 * @author duhongjun
 */
public final class SQLServerAddIndexExtractor implements OptionalSQLSegmentExtractor {
    
    private final IndexNameExtractor indexNameExtractor = new IndexNameExtractor();
    
    @Override
    public Optional<IndexSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> indexDefOptionNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ADD_COLUMN);
        if (!indexDefOptionNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> indexNameNode = ExtractorUtils.findFirstChildNode(indexDefOptionNode.get(), RuleName.INDEX_NAME);
        if (!indexNameNode.isPresent()) {
            return Optional.absent();
        }
        return indexNameExtractor.extract(indexNameNode.get());
    }
}
