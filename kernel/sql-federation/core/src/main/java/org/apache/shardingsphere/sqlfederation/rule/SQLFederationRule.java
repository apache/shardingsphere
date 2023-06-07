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

package org.apache.shardingsphere.sqlfederation.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;

/**
 * SQL federation rule.
 */
public final class SQLFederationRule implements GlobalRule {
    
    @Getter
    private final SQLFederationRuleConfiguration configuration;
    
    public SQLFederationRule(final SQLFederationRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
    }
    
    /**
     * Get SQL federation executor.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param metaData ShardingSphere meta data
     * @param shardingSphereData ShardingSphere data
     * @param jdbcExecutor jdbc executor
     * @return created instance
     */
    public SQLFederationExecutor getSQLFederationExecutor(final String databaseName, final String schemaName, final ShardingSphereMetaData metaData, final ShardingSphereData shardingSphereData,
                                                          final JDBCExecutor jdbcExecutor) {
        SQLFederationExecutor result = TypedSPILoader.getService(SQLFederationExecutor.class, "ADVANCED");
        result.init(databaseName, schemaName, metaData, shardingSphereData, jdbcExecutor);
        return result;
    }
    
    @Override
    public String getType() {
        return SQLFederationRule.class.getSimpleName();
    }
}
