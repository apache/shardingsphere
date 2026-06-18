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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collections;

/**
 * Create sharding key generator executor.
 */
@Setter
public final class CreateShardingKeyGeneratorExecutor implements DatabaseRuleCreateExecutor<CreateShardingKeyGeneratorStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateShardingKeyGeneratorStatement sqlStatement) {
        TypedSPILoader.checkService(KeyGenerateAlgorithm.class, sqlStatement.getAlgorithmSegment().getName(), sqlStatement.getAlgorithmSegment().getProps());
        if (!sqlStatement.isIfNotExists()) {
            checkExist(sqlStatement);
        }
    }
    
    private void checkExist(final CreateShardingKeyGeneratorStatement sqlStatement) {
        if (null == rule) {
            return;
        }
        ShardingSpherePreconditions.checkState(!rule.getConfiguration().getKeyGenerators().containsKey(sqlStatement.getName()),
                () -> new DuplicateRuleException("sharding key generator", database.getName(), Collections.singleton(sqlStatement.getName())));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingKeyGeneratorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        if (null != rule && sqlStatement.isIfNotExists() && rule.getConfiguration().getKeyGenerators().containsKey(sqlStatement.getName())) {
            return result;
        }
        result.getKeyGenerators().put(sqlStatement.getName(), new AlgorithmConfiguration(sqlStatement.getAlgorithmSegment().getName(), sqlStatement.getAlgorithmSegment().getProps()));
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<CreateShardingKeyGeneratorStatement> getType() {
        return CreateShardingKeyGeneratorStatement.class;
    }
}
