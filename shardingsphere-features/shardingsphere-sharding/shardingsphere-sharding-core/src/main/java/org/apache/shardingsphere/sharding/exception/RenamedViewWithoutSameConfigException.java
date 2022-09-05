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

package org.apache.shardingsphere.sharding.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Unsupported rename view exception.
 */
public final class RenamedViewWithoutSameConfigException extends ShardingSphereSQLException {
    
    private static final long serialVersionUID = -8276278970848162458L;
    
    public RenamedViewWithoutSameConfigException() {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 20053, "Alter view rename .. to .. statement should have same config for origin view and new view");
    }
}
