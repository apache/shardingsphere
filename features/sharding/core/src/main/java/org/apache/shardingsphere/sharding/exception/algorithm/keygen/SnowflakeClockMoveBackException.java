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

package org.apache.shardingsphere.sharding.exception.algorithm.keygen;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sharding.exception.ShardingSQLException;

/**
 * Snowflake clock move back exception.
 */
public final class SnowflakeClockMoveBackException extends ShardingSQLException {
    
    private static final long serialVersionUID = -2435731376659956566L;
    
    public SnowflakeClockMoveBackException(final long lastMillis, final long currentMillis) {
        super(XOpenSQLState.GENERAL_ERROR, 92, "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds.", lastMillis, currentMillis);
    }
}
