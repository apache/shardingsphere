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

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.GroupBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.OrderBySegment;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;

/**
 * Group by clause extractor.
 *
 * @author duhongjun
 */
public final class GroupByClauseExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<GroupBySegment> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> orderByParentNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.GROUP_BY_CLAUSE);
        if (!orderByParentNode.isPresent()) {
            return Optional.absent();
        }
        GroupBySegment result = new GroupBySegment(orderByParentNode.get().getStop().getStopIndex() + 2);
        result.getGroupByItems().addAll(new OrderByClauseExtractor().extractOrderBy(orderByParentNode.get()));
        return Optional.of(result);
    }
    
    protected OrderBySegment buildSegment(final String ownerName, final String name, final int index, final OrderDirection orderDirection,
                                          final int orderTokenBeginPosition, final int columnBeginPosition) {
        return new OrderBySegment(Optional.of(ownerName), Optional.of(name), columnBeginPosition, index, new OrderByToken(orderTokenBeginPosition));
    }
    
}
