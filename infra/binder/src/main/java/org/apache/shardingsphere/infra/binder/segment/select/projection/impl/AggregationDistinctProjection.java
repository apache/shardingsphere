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

package org.apache.shardingsphere.infra.binder.segment.select.projection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * Aggregation distinct projection.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public final class AggregationDistinctProjection extends AggregationProjection {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String distinctInnerExpression;
    
    public AggregationDistinctProjection(final int startIndex, final int stopIndex, final AggregationType type, final String innerExpression,
                                         final IdentifierValue alias, final String distinctInnerExpression, final DatabaseType databaseType) {
        super(type, innerExpression, alias, databaseType);
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.distinctInnerExpression = distinctInnerExpression;
    }
    
    /**
     * Get distinct column label.
     *
     * @return distinct column label
     */
    public String getDistinctColumnLabel() {
        return getAlias().map(IdentifierValue::getValue).orElse(distinctInnerExpression);
    }
    
    @Override
    public Projection transformSubqueryProjection(final IdentifierValue subqueryTableAlias, final IdentifierValue originalOwner, final IdentifierValue originalName) {
        return getAlias().isPresent() ? new ColumnProjection(subqueryTableAlias, getAlias().get(), null)
                : new AggregationDistinctProjection(startIndex, stopIndex, getType(), getInnerExpression(), null, distinctInnerExpression, getDatabaseType());
    }
}
