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

package io.shardingsphere.core.parsing.antlr.sql.segment.order;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import lombok.Getter;

/**
 * Order by item segment.
 * 
 * @author duhongjun
 */
@Getter
public final class OrderByItemSegment implements SQLSegment {
    
    private final int index;
    
    private final int expressionStartPosition;
    
    private final int expressionEndPosition;
    
    private final boolean isIdentifier;
    
    private final OrderByToken orderByToken;
    
    private final OrderDirection orderDirection;
    
    private final OrderDirection nullOrderDirection;
    
    public OrderByItemSegment(final int index, final int expressionStartPosition, final int expressionEndPosition, final boolean isIdentifier,
                              final OrderByToken orderByToken, final OrderDirection orderDirection, final OrderDirection nullOrderDirection) {
        this.index = index;
        this.expressionStartPosition = expressionStartPosition;
        this.expressionEndPosition = expressionEndPosition;
        this.isIdentifier = isIdentifier;
        this.orderByToken = orderByToken;
        this.orderDirection = orderDirection;
        this.nullOrderDirection = nullOrderDirection;
    }
    
    public OrderByItemSegment(final int index, final int expressionStartPosition, final int endPosition, final boolean isIdentifier, final OrderByToken orderByToken) {
        this(index, expressionStartPosition, endPosition, isIdentifier, orderByToken, OrderDirection.ASC, OrderDirection.ASC);
    }
}
