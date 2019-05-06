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
import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.antlr.extractor.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.ColumnNameOrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.ExpressionOrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.IndexOrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.OrderByItemSegment;
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
            OrderDirection orderDirection = 2 == each.getChildCount() && OrderDirection.DESC.name().equalsIgnoreCase(each.getChild(1).getText()) ? OrderDirection.DESC : OrderDirection.ASC;
            Optional<ParserRuleContext> indexNode = ExtractorUtils.findFirstChildNode(each, RuleName.NUMBER);
            if (indexNode.isPresent()) {
                result.add(new IndexOrderByItemSegment(NumberUtil.getExactlyNumber(indexNode.get().getText(), 10).intValue(), orderDirection, OrderDirection.ASC));
                continue;
            }
            Optional<ParserRuleContext> expressionNode = ExtractorUtils.findFirstChildNode(each, RuleName.EXPR);
            if (expressionNode.isPresent()) {
                result.add(new ExpressionOrderByItemSegment(expressionNode.get().getText(), orderDirection, OrderDirection.ASC));
                continue;
            }
            Optional<ParserRuleContext> columnNameNode = ExtractorUtils.findFirstChildNode(each, RuleName.COLUMN_NAME);
            if (columnNameNode.isPresent()) {
                result.add(new ColumnNameOrderByItemSegment(columnNameNode.get().getText(), ((ParserRuleContext) each.getChild(0)).getStart().getStartIndex(), orderDirection, OrderDirection.ASC));
            }
        }
        return result;
    }
}
