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

package io.shardingsphere.core.parsing.antlr.sql.segment;

import com.google.common.base.Optional;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import lombok.Getter;

/**
 * Order by segment.
 * 
 * @author duhongjun
 */
@Getter
public final class OrderBySegment implements SQLSegment {
    
    private final Optional<String> owner;
    
    private final Optional<String> name;
    
    private final int index;
    
    private final int startPosition;
    
    private final OrderByToken orderByToken;
    
    private final OrderDirection orderDirection;
    
    private final OrderDirection nullOrderDirection;
    
    public OrderBySegment(final Optional<String> owner, final Optional<String> name, final int index, final int startPosition, final OrderByToken orderByToken, final OrderDirection orderDirection,
                          final OrderDirection nullOrderDirection) {
        this.owner = owner;
        this.name = name;
        this.index = index;
        this.startPosition = startPosition;
        this.orderByToken = orderByToken;
        this.orderDirection = orderDirection;
        this.nullOrderDirection = nullOrderDirection;
    }
    
    public OrderBySegment(final Optional<String> owner, final Optional<String> name, final int index, final int startPosition, final OrderByToken orderByToken) {
        this(owner, name, index, startPosition, orderByToken, OrderDirection.ASC, OrderDirection.ASC);
    }
}
