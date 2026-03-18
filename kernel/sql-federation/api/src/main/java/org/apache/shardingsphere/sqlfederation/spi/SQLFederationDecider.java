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

package org.apache.shardingsphere.sqlfederation.spi;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPI;

import java.util.Collection;
import java.util.List;

/**
 * SQL federation decider.
 * 
 * @param <T> type of rule
 */
@SingletonSPI
public interface SQLFederationDecider<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Judge whether to use SQL federation.
     *
     * @param sqlStatementContext sql statement context
     * @param parameters parameters
     * @param globalRuleMetaData global rule meta data
     * @param database database
     * @param rule rule
     * @param includedDataNodes included data nodes
     * @return use SQL federation or not
     */
    boolean decide(SQLStatementContext sqlStatementContext, List<Object> parameters,
                   RuleMetaData globalRuleMetaData, ShardingSphereDatabase database, T rule, Collection<DataNode> includedDataNodes);
}
