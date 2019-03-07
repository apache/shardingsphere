/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.parsing.antlr.sql.AliasAvailable;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.order.GroupBySegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.SelectItemSegment;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.util.SQLUtil;

/**
 * Subquery expression segment.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SubquerySegment implements SelectItemSegment, ExpressionSegment, AliasAvailable {
    
    private final boolean subqueryInFrom;
    
    private SelectClauseSegment selectClauseSegment;
    
    private FromWhereSegment fromWhereSegment;
    
    private GroupBySegment groupBySegment;
    
    private OrderBySegment orderBySegment;
    
    private String alias;
    
    private final int startIndex;
    
    private final int stopIndex; 
    
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
    
    @Override
    public Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }
    
    @Override
    public void setAlias(final String alias) {
        this.alias = SQLUtil.getExactlyValue(alias);
    }

    @Override
    public Optional<SQLExpression> convertToSQLExpression(final String sql) {
        return Optional.absent();
    }
}
