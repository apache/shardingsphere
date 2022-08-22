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

package org.apache.shardingsphere.infra.binder.decider;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPI;

/**
 * SQL federation decider.
 * 
 * @param <T> type of rule
 */
public interface SQLFederationDecider<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Judge whether to use sql federation engine.
     *
     * @param deciderContext decider context
     * @param queryContext logic SQL
     * @param database database metadata
     * @param rule rule
     * @param props props
     */
    void decide(SQLFederationDeciderContext deciderContext, QueryContext queryContext, ShardingSphereDatabase database, T rule, ConfigurationProperties props);
}
