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

package org.apache.shardingsphere.core.parse.core.extractor;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.constraint.DropPrimaryKeySegment;

import java.util.Map;

/**
 * Drop primary key extractor for Oracle.
 *
 * @author duhongjun
 */
public final class OracleDropPrimaryKeyExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<DropPrimaryKeySegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> dropConstraintNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.DROP_CONSTRAINT_CLAUSE);
        if (!dropConstraintNode.isPresent()) {
            return Optional.absent();
        }
        return ExtractorUtils.findFirstChildNode(dropConstraintNode.get(), RuleName.PRIMARY_KEY).isPresent()
                ? Optional.of(new DropPrimaryKeySegment(dropConstraintNode.get().getStart().getStartIndex(), dropConstraintNode.get().getStop().getStopIndex()))
                : Optional.<DropPrimaryKeySegment>absent();
    }
}
