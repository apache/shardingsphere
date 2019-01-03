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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.constraint.dialect.oracle;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.constraint.DropPrimaryKeySegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Drop primary key extractor for Oracle.
 *
 * @author duhongjun
 */
public final class OracleDropPrimaryKeyExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<DropPrimaryKeySegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> dropConstraintNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.DROP_CONSTRAINT_CLAUSE);
        if (!dropConstraintNode.isPresent()) {
            return Optional.absent();
        }
        return ExtractorUtils.findFirstChildNode(dropConstraintNode.get(), RuleName.PRIMARY_KEY).isPresent() ? Optional.of(new DropPrimaryKeySegment()) : Optional.<DropPrimaryKeySegment>absent();
    }
}
