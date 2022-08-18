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

package org.apache.shardingsphere.infra.federation.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.federation.executor.original.OriginalFederationExecutor;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;

/**
 * Federation executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FederationExecutorFactory {
    
    /**
     * Create new instance of federation executor factory.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param globalRuleMetaData global rule meta data
     * @param optimizerContext filterable optimizer context
     * @param props configuration properties
     * @param jdbcExecutor jdbc executor
     * @param eventBusContext event bus context
     * @return created instance
     */
    public static FederationExecutor newInstance(final String databaseName, final String schemaName, final OptimizerContext optimizerContext,
                                                 final ShardingSphereRuleMetaData globalRuleMetaData, final ConfigurationProperties props, final JDBCExecutor jdbcExecutor,
                                                 final EventBusContext eventBusContext) {
        // TODO Consider about AdvancedFederationExecutor and TranslatableFederationExecutor
        return new OriginalFederationExecutor(databaseName, schemaName, optimizerContext, globalRuleMetaData, props, jdbcExecutor, eventBusContext);
    }
}
