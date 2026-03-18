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

package org.apache.shardingsphere.sharding.exception.strategy;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sharding.exception.ShardingSQLException;

/**
 * Invalid sharding strategy configuration exception.
 */
public final class InvalidShardingStrategyConfigurationException extends ShardingSQLException {
    
    private static final long serialVersionUID = -5874317771225005670L;
    
    public InvalidShardingStrategyConfigurationException(final String strategyLevel, final String strategyType) {
        super(XOpenSQLState.GENERAL_ERROR, 60, "Invalid %s strategy '%s', strategy does not match data nodes.", strategyLevel, strategyType);
    }
}
