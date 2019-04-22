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

package org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.antlr.sql.AliasAvailable;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Subquery expression segment.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SubquerySegment implements SelectItemSegment, ExpressionSegment, AliasAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final boolean subqueryInFrom;
    
    private SelectItemsSegment selectItemsSegment;
    
    private WhereSegment whereSegment;
    
    private GroupBySegment groupBySegment;
    
    private OrderBySegment orderBySegment;
    
    private String alias;
    
    /**
     * Get select items segment.
     * 
     * @return select items segment
     */
    public Optional<SelectItemsSegment> getSelectItemsSegment() {
        return Optional.fromNullable(selectItemsSegment);
    }
    
    /**
     * Get from where segment.
     *
     * @return from where segment
     */
    public Optional<WhereSegment> getWhereSegment() {
        return Optional.fromNullable(whereSegment);
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
    public SQLExpression getSQLExpression(final String sql) {
        return new SQLIgnoreExpression(sql.substring(startIndex, startIndex + 1));
    }
}
