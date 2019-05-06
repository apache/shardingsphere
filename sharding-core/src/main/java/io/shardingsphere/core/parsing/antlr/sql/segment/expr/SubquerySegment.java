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

package io.shardingsphere.core.parsing.antlr.sql.segment.expr;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.GroupBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Subquery expression segment.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SubquerySegment extends ExpressionWithAliasSegment {
    
    private final boolean subqueryInFrom;
    
    private SelectClauseSegment selectClauseSegment;
    
    private FromWhereSegment fromWhereSegment;
    
    private GroupBySegment groupBySegment;
    
    private OrderBySegment orderBySegment;
    
    /**
     * Get select clause segment.
     * 
     * @return select clause segment
     */
    public Optional<SelectClauseSegment> getSelectClauseSegment() {
        return Optional.fromNullable(selectClauseSegment);
    }
    
    /**
     * Get from where segment.
     *
     * @return from where segment
     */
    public Optional<FromWhereSegment> getFromWhereSegment() {
        return Optional.fromNullable(fromWhereSegment);
    }
    
    /**
     * Get group by segment.
     *
     * @return group by segment
     */
    public Optional<GroupBySegment> getGroupBySegment() {
        return Optional.fromNullable(groupBySegment);
    }
    
    /**
     * Get order by segment.
     *
     * @return order by segment
     */
    public Optional<OrderBySegment> getOrderBySegment() {
        return Optional.fromNullable(orderBySegment);
    }
}
