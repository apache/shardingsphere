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

package io.shardingsphere.core.parsing.antlr.extractor.segment.impl;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.GroupBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;

/**
 * Subquery extractor.
 * 
 * @author duhongjun
 */
public final class SubqueryExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SubquerySegment> extract(ParserRuleContext ancestorNode) {
        boolean subqueryInFrom = false;
        ParserRuleContext parentNode = ancestorNode.getParent();
        while(null != parentNode) {
            if(RuleName.FROM_CLAUSE.getName().equals(parentNode.getClass().getSimpleName())) {
                subqueryInFrom = true;
                break;
            }
            parentNode = parentNode.getParent();
        }
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SUBQUERY);
        if (!subqueryNode.isPresent()) {
            return Optional.absent();
        }
        Optional<SelectClauseSegment> selectClauseSegment = new SelectClauseExtractor().extract(subqueryNode.get());
        Optional<FromWhereSegment> fromWhereSegment = new FromWhereExtractor().extract(subqueryNode.get());
        Optional<GroupBySegment> groupBySegment = new GroupByExtractor().extract(subqueryNode.get());
        Optional<OrderBySegment> orderBySegment = new OrderByExtractor().extract(subqueryNode.get());
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SUBQUERY);
        Optional<String> alias = Optional.absent();
        if(aliasNode.isPresent()) {
            alias = Optional.of(aliasNode.get().getText());
        }
        return Optional.of(new SubquerySegment(selectClauseSegment, fromWhereSegment, groupBySegment, orderBySegment, alias, subqueryInFrom)) ;
    }
}
