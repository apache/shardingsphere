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
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Order by extractor.
 *
 * @author duhongjun
 */
public final class OrderByExtractor implements OptionalSQLSegmentExtractor {
    
    private final OrderByItemExtractor orderByItemExtractor = new OrderByItemExtractor();
    
    @Override
    public Optional<OrderBySegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> orderByNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ORDER_BY_CLAUSE);
        if (!orderByNode.isPresent()) {
            return Optional.absent();
        }
        OrderBySegment result = new OrderBySegment();
        result.getOrderByItems().addAll(orderByItemExtractor.extract(orderByNode.get()));
        return Optional.of(result);
    }
}
