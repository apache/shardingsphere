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

package org.apache.shardingsphere.driver.executor.callback.replica;

import org.apache.shardingsphere.driver.executor.callback.RuleExecuteBatchExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.replica.execute.executor.ReplicaSQLExecutorCallback;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL executor callback for execute batch for replica.
 */
public final class ReplicaExecuteBatchExecutorCallback extends ReplicaSQLExecutorCallback<int[]> implements RuleExecuteBatchExecutorCallback<ReplicaRule> {
    
    @Override
    public Collection<int[]> execute(final Collection<StatementExecuteUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        Collection<int[]> result = new LinkedList<>();
        // TODO
        return result;
    }
}
