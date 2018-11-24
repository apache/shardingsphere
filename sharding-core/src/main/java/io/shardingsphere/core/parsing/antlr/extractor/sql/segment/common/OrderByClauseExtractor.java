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

package io.shardingsphere.core.parsing.antlr.extractor.sql.segment.common;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.base.Optional;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.GroupByExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.OrderByExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.util.ASTUtils;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;

/**
 * Order by clause extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public class OrderByClauseExtractor implements CollectionSQLSegmentExtractor<OrderByExtractResult> {
    
    private final RuleName ruleName;
    
    public OrderByClauseExtractor() {
        ruleName = RuleName.ORDER_BY_CLAUSE;
    }
    
    @Override
    public Collection<OrderByExtractResult> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> orderByParentNode = ASTUtils.findFirstChildNode(ancestorNode, ruleName);
        if (!orderByParentNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<ParserRuleContext> orderByNodes = ASTUtils.getAllDescendantNodes(orderByParentNode.get(), RuleName.ORDER_BY_ITEM);
        if (orderByNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<OrderByExtractResult> result = new LinkedList<>();
        for (ParserRuleContext each : orderByNodes) {
            int count = each.getChildCount();
            if (count == 0) {
                continue;
            }
            Optional<ParserRuleContext> numberNode = ASTUtils.findFirstChildNode(each, RuleName.NUMBER);
            int index = -1;
            if(numberNode.isPresent()) {
                index = NumberUtil.getExactlyNumber(numberNode.get().getText(), 10).intValue();
            }
            String name = each.getChild(0).getText();
            String ownerName = "";
            int pos = name.lastIndexOf(".");
            OrderDirection orderDirection = null;
            if (0 < pos) {
                ownerName = name.substring(0, pos - 1);
                name = name.substring(pos + 1);
            }
            if (1 < count) {
                TerminalNode direction = (TerminalNode) each.getChild(1);
                if (direction.getSymbol().getStopIndex() - direction.getSymbol().getStartIndex() == 3) {
                    orderDirection = OrderDirection.DESC;
                } else {
                    orderDirection = OrderDirection.ASC;
                }
            }
            result.add((OrderByExtractResult)buildExtractResult(ownerName, name, index, orderDirection, each.getStart().getStartIndex()));
        }
        return result;
    }
    
    protected GroupByExtractResult buildExtractResult(String ownerName, String name, int index, OrderDirection orderDirection, int orderTokenBeginPosition) {
        return new OrderByExtractResult(Optional.of(ownerName), Optional.of(name), index, orderDirection, OrderDirection.ASC, new OrderByToken(orderTokenBeginPosition));
    }
}
