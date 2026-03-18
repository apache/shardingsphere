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

/**
 * Mismatched complex inline sharding algorithm's column and value exception.
 */
public final class MismatchedComplexInlineShardingAlgorithmColumnAndValueSizeException extends ShardingSQLException {
    
    private static final long serialVersionUID = -3667110081810167498L;
    
    public MismatchedComplexInlineShardingAlgorithmColumnAndValueSizeException(final int shardingColumnSize, final int shardingValueSize) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 53, "Complex inline algorithm need %d sharding columns, but only found %d.", shardingColumnSize, shardingValueSize);
    }
}
