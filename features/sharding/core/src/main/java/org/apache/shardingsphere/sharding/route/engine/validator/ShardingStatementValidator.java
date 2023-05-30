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

package org.apache.shardingsphere.sharding.route.engine.validator;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.List;

/**
 * Sharding statement validator.
 */
public interface ShardingStatementValidator {
    
    /**
     * Validate whether sharding operation is supported before route.
     * 
     * @param shardingRule sharding rule
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param database database
     * @param props props
     */
    void preValidate(ShardingRule shardingRule, SQLStatementContext sqlStatementContext, List<Object> params, ShardingSphereDatabase database, ConfigurationProperties props);
    
    /**
     * Validate whether sharding operation is supported after route.
     * 
     * @param shardingRule sharding rule
     * @param sqlStatementContext SQL statement context
     * @param hintValueContext hint value context
     * @param params SQL parameters
     * @param database database
     * @param props props
     * @param routeContext route context
     */
    void postValidate(ShardingRule shardingRule, SQLStatementContext sqlStatementContext, HintValueContext hintValueContext, List<Object> params,
                      ShardingSphereDatabase database, ConfigurationProperties props, RouteContext routeContext);
}
