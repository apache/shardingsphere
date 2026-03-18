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

package org.apache.shardingsphere.sharding.exception.algorithm;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sharding.exception.ShardingSQLException;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;

import java.util.Collection;

/**
 * Mismatched sharding data source route info exception.
 */
public final class MismatchedShardingDataSourceRouteInfoException extends ShardingSQLException {
    
    private static final long serialVersionUID = -345707079477626285L;
    
    public MismatchedShardingDataSourceRouteInfoException(final Collection<String> routeDataSourceNames, final Collection<String> actualDataSourceNames,
                                                          final Collection<ShardingConditionValue> shardingConditionValues) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 55,
                "Some routed data sources do not belong to configured data sources. routed data sources '%s', configured data sources '%s', sharding condition values '%s'.",
                routeDataSourceNames, actualDataSourceNames, shardingConditionValues);
    }
}
