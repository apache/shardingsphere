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

/**
 * Subquery expression segment.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
@Getter
public final class SubquerySegment implements ExpressionSegment {
    
    private final Optional<SelectClauseSegment> selectClauseSegment;
    
    private final Optional<FromWhereSegment> fromWhereSegment;
    
    private final Optional<GroupBySegment> groupBySegment;
    
    private final Optional<OrderBySegment> orderBySegment;
    
    private final Optional<String> alias;
    
    private final boolean subqueryInFrom;
}
