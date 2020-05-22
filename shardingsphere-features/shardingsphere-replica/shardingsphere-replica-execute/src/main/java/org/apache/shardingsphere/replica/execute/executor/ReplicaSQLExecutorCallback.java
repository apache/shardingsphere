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

package org.apache.shardingsphere.replica.execute.executor;

import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.executor.SQLExecutorCallback;
import org.apache.shardingsphere.infra.spi.order.OrderedSPI;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

/**
 * SQL executor callback for replica.
 * 
 * @param <T> class type of return value
 */
public abstract class ReplicaSQLExecutorCallback<T> implements SQLExecutorCallback<T>, OrderedSPI<ReplicaRule> {
    
    @Override
    public final int getOrder() {
        return 5;
    }
    
    @Override
    public final Class<ReplicaRule> getTypeClass() {
        return ReplicaRule.class;
    }
}
