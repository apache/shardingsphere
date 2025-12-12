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

package org.apache.shardingsphere.sharding.merge;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMergerEngine;
import org.apache.shardingsphere.infra.merge.engine.merger.impl.TransparentResultMerger;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.merge.dal.ShardingDALResultMerger;
import org.apache.shardingsphere.sharding.merge.ddl.ShardingDDLResultMerger;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

/**
 * Result merger engine for sharding.
 */
public final class ShardingResultMergerEngine implements ResultMergerEngine<ShardingRule> {
    
    @Override
    public ResultMerger newInstance(final String databaseName, final DatabaseType protocolType, final ShardingRule shardingRule, final ConfigurationProperties props,
                                    final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return new ShardingDQLResultMerger(protocolType);
        }
        if (sqlStatementContext.getSqlStatement() instanceof DDLStatement) {
            return new ShardingDDLResultMerger();
        }
        if (sqlStatementContext.getSqlStatement() instanceof DALStatement) {
            return new ShardingDALResultMerger(databaseName, shardingRule);
        }
        return new TransparentResultMerger();
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
