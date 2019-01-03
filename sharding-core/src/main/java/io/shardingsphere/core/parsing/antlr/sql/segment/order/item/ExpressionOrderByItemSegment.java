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

package io.shardingsphere.core.parsing.antlr.sql.segment.order.item;

import io.shardingsphere.core.constant.OrderDirection;
import lombok.Getter;

/**
 * Order by item segment for expression.
 * 
 * @author zhangliang
 */
@Getter
public final class ExpressionOrderByItemSegment extends OrderByItemSegment {
    
    private final String expression;
    
    public ExpressionOrderByItemSegment(final String expression, final OrderDirection orderDirection, final OrderDirection nullOrderDirection) {
        super(orderDirection, nullOrderDirection);
        this.expression = expression;
    }
}
