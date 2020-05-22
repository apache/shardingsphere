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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.callback.replica;

import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.callback.RuleProxySQLExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Replica SQL executor callback for Proxy.
 */
public final class ReplicaProxySQLExecutorCallback implements RuleProxySQLExecutorCallback<ReplicaRule> {
    
    @Override
    public Collection<ExecuteResponse> execute(final Collection<StatementExecuteUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        // TODO 
        return null;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public Class<ReplicaRule> getTypeClass() {
        return ReplicaRule.class;
    }
}
