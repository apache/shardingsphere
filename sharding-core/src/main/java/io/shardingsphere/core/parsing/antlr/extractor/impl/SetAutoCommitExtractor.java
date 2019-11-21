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

package io.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.tcl.SetAutoCommitSegment;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Set auto commit extractor.
 *
 * @author maxiaoguang
 */
public final class SetAutoCommitExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SetAutoCommitSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> autoCommitValueNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.AUTO_COMMIT_VALUE);
        return autoCommitValueNode.isPresent()
                ? Optional.of(new SetAutoCommitSegment("1".equals(SQLUtil.getExactlyValue(autoCommitValueNode.get().getText())))) : Optional.<SetAutoCommitSegment>absent();
    }
}
