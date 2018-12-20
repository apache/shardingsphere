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
import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Order by extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class OrderByExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<OrderBySegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> orderByParentNode = ExtractorUtils.findFirstChildNodeNoneRecursive(selectClauseNode.get().getParent(), RuleName.ORDER_BY_CLAUSE);
        if (!orderByParentNode.isPresent()) {
            return Optional.absent();
        }
        OrderBySegment result = new OrderBySegment();
        result.getOrderByItems().addAll(extractOrderBy(orderByParentNode.get()));
        return Optional.of(result);
    }
    
    protected Collection<OrderByItemSegment> extractOrderBy(final ParserRuleContext orderByParentNode) {
        Collection<ParserRuleContext> orderByNodes = ExtractorUtils.getAllDescendantNodes(orderByParentNode, RuleName.ORDER_BY_ITEM);
        if (orderByNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<OrderByItemSegment> result = new LinkedList<>();
        for (ParserRuleContext each : orderByNodes) {
            int count = each.getChildCount();
            if (count == 0) {
                continue;
            }
            int index = -1;
            Optional<ParserRuleContext> numberNode = ExtractorUtils.findFirstChildNode(each, RuleName.NUMBER);
            if (numberNode.isPresent()) {
                index = NumberUtil.getExactlyNumber(numberNode.get().getText(), 10).intValue();
            }
            boolean isIdentifier = RuleName.COLUMN_NAME.getName().equalsIgnoreCase(each.getChild(0).getClass().getSimpleName());
            OrderDirection orderDirection = OrderDirection.ASC;
            if (1 < count) {
                if (OrderDirection.DESC.name().equalsIgnoreCase(each.getChild(count - 1).getText())) {
                    orderDirection = OrderDirection.DESC;
                }
            }
            ParserRuleContext firstChild = (ParserRuleContext) each.getChild(0);
            result.add(buildSegment(index, orderDirection, firstChild.getStart().getStartIndex(), firstChild.getStop().getStopIndex(), isIdentifier, orderByParentNode.getStart().getStartIndex()));
        }
        return result;
    }
    
    private OrderByItemSegment buildSegment(final int index, final OrderDirection orderDirection, 
                                            final int expressionStartPosition, final int expressionEndPosition, final boolean isIdentifier, final int orderByItemStartPosition) {
        return new OrderByItemSegment(index, expressionStartPosition, expressionEndPosition, isIdentifier, new OrderByToken(orderByItemStartPosition), orderDirection, OrderDirection.ASC);
    }
}
