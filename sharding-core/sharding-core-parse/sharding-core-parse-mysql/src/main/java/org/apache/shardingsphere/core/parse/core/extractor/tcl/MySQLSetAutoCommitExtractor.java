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

package org.apache.shardingsphere.core.parse.core.extractor.tcl;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.Map;

/**
 * Set auto commit extractor for MySQL.
 *
 * @author maxiaoguang
 * @author zhangliang
 */
public final class MySQLSetAutoCommitExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<AutoCommitSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> autoCommitValueNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.AUTO_COMMIT_VALUE);
        return autoCommitValueNode.isPresent()
                ? Optional.of(new AutoCommitSegment(autoCommitValueNode.get().getStart().getStartIndex(), autoCommitValueNode.get().getStop().getStopIndex(), isAutoCommit(autoCommitValueNode.get())))
                : Optional.<AutoCommitSegment>absent();
    }
    
    private boolean isAutoCommit(final ParserRuleContext autoCommitValueNode) {
        String autoCommitValue = SQLUtil.getExactlyValue(autoCommitValueNode.getText());
        return "1".equals(autoCommitValue) || "ON".equals(autoCommitValue);
    }
}
