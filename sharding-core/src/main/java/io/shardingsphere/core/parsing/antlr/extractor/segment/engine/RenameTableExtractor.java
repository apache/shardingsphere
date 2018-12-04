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

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Rename table extractor.
 *
 * @author duhongjun
 */
public final class RenameTableExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SQLSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> renameTableNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.RENAME_TABLE);
        if (!renameTableNode.isPresent() || 0 == renameTableNode.get().getChildCount()) {
            return Optional.absent();
        }
        throw new SQLParsingUnsupportedException("Unsupported SQL statement of rename table");
    }
}
