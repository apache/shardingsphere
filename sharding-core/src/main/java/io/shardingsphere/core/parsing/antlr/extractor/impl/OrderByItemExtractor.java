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
import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderByItemSegment;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.util.NumberUtil;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Order by item extractor.
 *
 * @author zhangliang
 */
public final class OrderByItemExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<OrderByItemSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.ORDER_BY_ITEM)) {
            // FIXME when will count be zero?
            int count = each.getChildCount();
            if (0 == count) {
                continue;
            }
            Optional<ParserRuleContext> numberNode = ExtractorUtils.findFirstChildNode(each, RuleName.NUMBER);
            int index = numberNode.isPresent() ? NumberUtil.getExactlyNumber(numberNode.get().getText(), 10).intValue() : -1;
            boolean isIdentifier = RuleName.COLUMN_NAME.getName().equalsIgnoreCase(each.getChild(0).getClass().getSimpleName());
            OrderDirection orderDirection = count > 1 && OrderDirection.DESC.name().equalsIgnoreCase(each.getChild(count - 1).getText()) ? OrderDirection.DESC : OrderDirection.ASC;
            ParserRuleContext firstChild = (ParserRuleContext) each.getChild(0);
            result.add(new OrderByItemSegment(index, firstChild.getStart().getStartIndex(), firstChild.getStop().getStopIndex(), 
                    isIdentifier, new OrderByToken(ancestorNode.getStart().getStartIndex()), orderDirection, OrderDirection.ASC));
        }
        return result;
    }
}
