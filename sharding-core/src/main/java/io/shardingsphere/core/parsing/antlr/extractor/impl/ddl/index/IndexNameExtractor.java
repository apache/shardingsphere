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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.index;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.index.IndexSegment;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Index name extractor.
 * 
 * @author duhongjun
 */
public final class IndexNameExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<IndexSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> indexNameNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.INDEX_NAME);
        return indexNameNode.isPresent() ? Optional.of(new IndexSegment(new IndexToken(indexNameNode.get().getStop().getStartIndex(), SQLUtil.getNameWithoutSchema(indexNameNode.get().getText()))))
                : Optional.<IndexSegment>absent();
    }
}
