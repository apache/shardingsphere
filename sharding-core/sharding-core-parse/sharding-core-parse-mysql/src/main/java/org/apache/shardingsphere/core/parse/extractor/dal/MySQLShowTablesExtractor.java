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

package org.apache.shardingsphere.core.parse.extractor.dal;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dal.ShowTablesSegment;

import java.util.Map;

/**
 * Show tables extractor for MySQL.
 * 
 * @author zhangliang
 */
public final class MySQLShowTablesExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ShowTablesSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> fromSchemaNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.FROM_SCHEMA);
        return Optional.of(fromSchemaNode.isPresent()
                ? new ShowTablesSegment(fromSchemaNode.get().getStart().getStartIndex(), fromSchemaNode.get().getStop().getStopIndex()) : new ShowTablesSegment());
    }
}
