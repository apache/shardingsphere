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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;

import java.util.Optional;

/**
 * Order by item segment.
 */
@RequiredArgsConstructor
@Getter
public abstract class OrderByItemSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final OrderDirection orderDirection;
    
    private final NullsOrderType nullsOrderType;
    
    /**
     * Get nulls order type.
     *
     * @return nulls order type
     */
    public Optional<NullsOrderType> getNullsOrderType() {
        return Optional.ofNullable(nullsOrderType);
    }
    
    /**
     * Get nulls order type.
     *
     * @param databaseType database type
     * @return nulls order type
     */
    public NullsOrderType getNullsOrderType(final DatabaseType databaseType) {
        if (null != nullsOrderType) {
            return nullsOrderType;
        }
        return new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getDefaultNullsOrderType().getResolvedOrderType(orderDirection.name());
    }
}
