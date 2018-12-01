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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.antlr.extractor.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.OrderBySegment;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;

/**
 * Order by clause extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public class OrderByClauseExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<OrderBySegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> orderByParentNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.ORDER_BY_CLAUSE);
        if (!orderByParentNode.isPresent()) {
            return Collections.emptyList();
        }
        return extractOrderBy(orderByParentNode.get());
    }
    
    public Collection<OrderBySegment> extractOrderBy(ParserRuleContext orderByParentNode) {
        Collection<ParserRuleContext> orderByNodes = ASTUtils.getAllDescendantNodes(orderByParentNode, RuleName.ORDER_BY_ITEM);
        if (orderByNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<OrderBySegment> result = new LinkedList<>();
        for (ParserRuleContext each : orderByNodes) {
            int count = each.getChildCount();
            if (count == 0) {
                continue;
            }
            Optional<ParserRuleContext> numberNode = ASTUtils.findFirstChildNode(each, RuleName.NUMBER);
            int index = -1;
            if (numberNode.isPresent()) {
                index = NumberUtil.getExactlyNumber(numberNode.get().getText(), 10).intValue();
            }
            String columnText = each.getChild(0).getText();
            int pos = columnText.lastIndexOf(".");
            OrderDirection orderDirection = null;
            Optional<String> owner = Optional.absent();
            Optional<String> name = Optional.absent();
            if (0 < pos) {
                owner = Optional.of(columnText.substring(0, pos));
                name = Optional.of(columnText.substring(pos + 1));
            }else {
                name = Optional.of(columnText);
            }
            orderDirection = OrderDirection.ASC;
            if (1 < count) {
                if (OrderDirection.DESC.name().equalsIgnoreCase(each.getChild(count - 1).getText())) {
                    orderDirection = OrderDirection.DESC;
                }
            }
            result.add(buildSegment(owner, name, index, orderDirection, each.getStart().getStartIndex(), orderByParentNode.getStart().getStartIndex()));
        }
        return result;
    }
    
    protected OrderBySegment buildSegment(final Optional<String> ownerName, final Optional<String> name, final int index, 
            final OrderDirection orderDirection, final int orderTokenBeginPosition, final int columnBeginPosition) {
        return new OrderBySegment(ownerName, name, index, columnBeginPosition, new OrderByToken(orderTokenBeginPosition),  orderDirection, OrderDirection.ASC); 
    }
}
