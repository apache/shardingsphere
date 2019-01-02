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

package io.shardingsphere.core.parsing.antlr.extractor.impl.dql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.FromWhereExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.GroupBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Subquery extractor.
 *
 * @author duhongjun
 */
public final class SubqueryExtractor implements OptionalSQLSegmentExtractor {
    
    private final FromWhereExtractor fromWhereExtractor = new FromWhereExtractor();
    
    private final GroupByExtractor groupByExtractor = new GroupByExtractor();
    
    private final OrderByExtractor orderByExtractor = new OrderByExtractor();
    
    @Override
    public Optional<SubquerySegment> extract(final ParserRuleContext subqueryNode) {
        if (!RuleName.SUBQUERY.getName().endsWith(subqueryNode.getClass().getSimpleName())) {
            return Optional.absent();
        }
        boolean subqueryInFrom = false;
        ParserRuleContext parentNode = subqueryNode.getParent();
        while (null != parentNode) {
            if (RuleName.FROM_CLAUSE.getName().equals(parentNode.getClass().getSimpleName())) {
                subqueryInFrom = true;
                break;
            }
            parentNode = parentNode.getParent();
        }
        SubquerySegment result = new SubquerySegment(subqueryInFrom);
        Optional<SelectClauseSegment> selectClauseSegment = new SelectClauseExtractor().extract(subqueryNode);
        if (selectClauseSegment.isPresent()) {
            result.setSelectClauseSegment(selectClauseSegment.get());
        }
        Optional<FromWhereSegment> fromWhereSegment = fromWhereExtractor.extract(subqueryNode);
        if (fromWhereSegment.isPresent()) {
            result.setFromWhereSegment(fromWhereSegment.get());
        }
        Optional<GroupBySegment> groupBySegment = groupByExtractor.extract(subqueryNode);
        if (groupBySegment.isPresent()) {
            result.setGroupBySegment(groupBySegment.get());
        }
        Optional<OrderBySegment> orderBySegment = orderByExtractor.extract(subqueryNode);
        if (orderBySegment.isPresent()) {
            result.setOrderBySegment(orderBySegment.get());
        }
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(subqueryNode.getParent(), RuleName.ALIAS);
        if (aliasNode.isPresent()) {
            result.setAlias(aliasNode.get().getText());
        }
        return Optional.of(result);
    }
}
