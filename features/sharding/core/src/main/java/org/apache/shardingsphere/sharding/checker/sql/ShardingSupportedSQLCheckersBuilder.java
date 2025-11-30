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

package org.apache.shardingsphere.sharding.checker.sql;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckersBuilder;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingAlterIndexSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingAlterTableSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingAlterViewSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingCreateFunctionSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingCreateIndexSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingCreateProcedureSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingCreateTableSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingCreateViewSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingDropIndexSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingDropTableSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.ddl.ShardingRenameTableSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.dml.ShardingInsertSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.dml.ShardingMultipleTablesSupportedChecker;
import org.apache.shardingsphere.sharding.checker.sql.dml.ShardingTableSupportedChecker;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Sharding SQL supported checker factory.
 */
public final class ShardingSupportedSQLCheckersBuilder implements SupportedSQLCheckersBuilder<ShardingRule> {
    
    private final Collection<SupportedSQLChecker<?, ShardingRule>> supportedSQLCheckers = Arrays.asList(
            new ShardingAlterIndexSupportedChecker(),
            new ShardingAlterTableSupportedChecker(),
            new ShardingAlterViewSupportedChecker(),
            new ShardingCreateFunctionSupportedChecker(),
            new ShardingCreateIndexSupportedChecker(),
            new ShardingCreateProcedureSupportedChecker(),
            new ShardingCreateTableSupportedChecker(),
            new ShardingCreateViewSupportedChecker(),
            new ShardingDropIndexSupportedChecker(),
            new ShardingDropTableSupportedChecker(),
            new ShardingRenameTableSupportedChecker(),
            new ShardingInsertSupportedChecker(),
            new ShardingTableSupportedChecker(),
            new ShardingMultipleTablesSupportedChecker());
    
    @HighFrequencyInvocation
    @Override
    public Collection<SupportedSQLChecker<?, ShardingRule>> getSupportedSQLCheckers() {
        return supportedSQLCheckers;
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
