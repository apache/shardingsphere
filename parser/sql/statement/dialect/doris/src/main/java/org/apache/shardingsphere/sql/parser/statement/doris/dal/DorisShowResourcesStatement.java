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

package org.apache.shardingsphere.sql.parser.statement.doris.dal;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowResourcesNameConditionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowResourcesResourceTypeConditionSegment;

import java.util.Optional;

/**
 * Show resources statement for Doris.
 */
@Getter
@Setter
public final class DorisShowResourcesStatement extends DALStatement {
    
    private ShowResourcesNameConditionSegment nameCondition;
    
    private ShowResourcesResourceTypeConditionSegment resourceTypeCondition;
    
    private ShowLikeSegment like;
    
    private OrderBySegment orderBy;
    
    private LimitSegment limit;
    
    public DorisShowResourcesStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get name condition segment.
     *
     * @return name condition segment
     */
    public Optional<ShowResourcesNameConditionSegment> getNameCondition() {
        return Optional.ofNullable(nameCondition);
    }
    
    /**
     * Get resource type condition segment.
     *
     * @return resource type condition segment
     */
    public Optional<ShowResourcesResourceTypeConditionSegment> getResourceTypeCondition() {
        return Optional.ofNullable(resourceTypeCondition);
    }
    
    /**
     * Get like pattern.
     *
     * @return like pattern
     */
    public Optional<ShowLikeSegment> getLike() {
        return Optional.ofNullable(like);
    }
    
    /**
     * Get order by.
     *
     * @return order by
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.ofNullable(orderBy);
    }
    
    /**
     * Get limit.
     *
     * @return limit
     */
    public Optional<LimitSegment> getLimit() {
        return Optional.ofNullable(limit);
    }
}
