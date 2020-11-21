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

package org.apache.shardingsphere.infra.executor.sql.group.driver.jdbc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.group.driver.StorageResourceOption;

import java.sql.ResultSet;

/**
 * Statement option.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class StatementOption implements StorageResourceOption {
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final boolean returnGeneratedKeys;
    
    public StatementOption(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        this(resultSetType, resultSetConcurrency, resultSetHoldability, false);
    }
    
    public StatementOption(final boolean returnGeneratedKeys) {
        this(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, returnGeneratedKeys);
    }
}
