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

package org.apache.shardingsphere.sharding.spi;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.util.spi.type.typed.algorithm.ShardingSphereAlgorithm;

import java.util.List;

/**
 * Sharding audit algorithm.
 */
public interface ShardingAuditAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Sharding audit algorithm SQL check.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param grantee grantee
     * @param globalRuleMetaData global rule meta data
     * @param database database
     */
    void check(SQLStatementContext sqlStatementContext, List<Object> params, Grantee grantee, ShardingSphereRuleMetaData globalRuleMetaData, ShardingSphereDatabase database);
}
