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

package org.apache.shardingsphere.sharding.route.engine.validator.dml.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CopyStatement;

import java.util.List;

/**
 * Sharding copy statement validator.
 */
public final class ShardingCopyStatementValidator extends ShardingDMLStatementValidator<CopyStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<CopyStatement> sqlStatementContext,
                            final List<Object> parameters, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        String tableName = sqlStatementContext.getSqlStatement().getTableSegment().getTableName().getIdentifier().getValue();
        if (shardingRule.isShardingTable(tableName)) {
            throw new UnsupportedShardingOperationException("COPY", tableName);
        }
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<CopyStatement> sqlStatementContext,
                             final List<Object> parameters, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
    }
}
