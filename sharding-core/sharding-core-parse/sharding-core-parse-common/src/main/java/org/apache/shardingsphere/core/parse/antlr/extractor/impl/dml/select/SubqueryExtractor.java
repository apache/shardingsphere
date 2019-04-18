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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.SubquerySegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.order.OrderBySegment;

import java.util.Map;

/**
 * Subquery extractor.
 *
 * @author duhongjun
 */
public final class SubqueryExtractor implements OptionalSQLSegmentExtractor {
    
    private final SelectWhereExtractor selectWhereExtractor = new SelectWhereExtractor();
    
    private final GroupByExtractor groupByExtractor = new GroupByExtractor();
    
    private final OrderByExtractor orderByExtractor = new OrderByExtractor();
    
    @Override
    public Optional<SubquerySegment> extract(final ParserRuleContext subqueryNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
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
        SubquerySegment result = new SubquerySegment(subqueryNode.getStart().getStartIndex(), subqueryNode.getStop().getStopIndex(), subqueryInFrom);
        Optional<SelectItemsSegment> selectItemsSegment = new SelectItemsExtractor().extract(subqueryNode, parameterMarkerIndexes);
        if (selectItemsSegment.isPresent()) {
            result.setSelectItemsSegment(selectItemsSegment.get());
        }
        Optional<WhereSegment> whereSegment = selectWhereExtractor.extract(subqueryNode, parameterMarkerIndexes);
        if (whereSegment.isPresent()) {
            result.setWhereSegment(whereSegment.get());
        }
        Optional<GroupBySegment> groupBySegment = groupByExtractor.extract(subqueryNode, parameterMarkerIndexes);
        if (groupBySegment.isPresent()) {
            result.setGroupBySegment(groupBySegment.get());
        }
        Optional<OrderBySegment> orderBySegment = orderByExtractor.extract(subqueryNode, parameterMarkerIndexes);
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
