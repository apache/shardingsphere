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

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.List;

/**
 * Sharding statement validator.
 *
 * @param <T> type of SQL statement
 */
public interface ShardingStatementValidator<T extends SQLStatement> {
    
    /**
     * Validate whether sharding operation is supported before route.
     * 
     * @param shardingRule sharding rule
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param metaData meta data
     */
    void preValidate(ShardingRule shardingRule, SQLStatementContext<T> sqlStatementContext, List<Object> parameters, ShardingSphereMetaData metaData);
    
    /**
     * Validate whether sharding operation is supported after route.
     *
     * @param sqlStatement SQL statement
     * @param routeContext route context
     */
    void postValidate(T sqlStatement, RouteContext routeContext);
}
