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

package org.apache.shardingsphere.infra.binder.decider;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL federation decide engine.
 */
public final class SQLFederationDecideEngine {
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLFederationDecider> deciders;
    
    private final boolean isFederationDisabled;
    
    public SQLFederationDecideEngine(final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) {
        deciders = OrderedSPILoader.getServices(SQLFederationDecider.class, rules);
        isFederationDisabled = "NONE".equals(props.getValue(ConfigurationPropertyKey.SQL_FEDERATION_TYPE));
    }
    
    /**
     * Decide use SQL federation or not.
     * 
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param database ShardingSphere database
     * @param globalRuleMetaData global rule meta data
     * @return use SQL federation or not
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean decide(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters,
                          final ShardingSphereDatabase database, final ShardingSphereRuleMetaData globalRuleMetaData) {
        // TODO BEGIN: move this logic to SQLFederationDecider implement class when we remove sql federation type
        if (isQuerySystemSchema(sqlStatementContext, database)) {
            return true;
        }
        // TODO END
        if (isFederationDisabled || !(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SQLFederationDeciderContext context = new SQLFederationDeciderContext();
        for (Entry<ShardingSphereRule, SQLFederationDecider> entry : deciders.entrySet()) {
            if (!context.isUseSQLFederation()) {
                entry.getValue().decide(context, (SelectStatementContext) sqlStatementContext, parameters, globalRuleMetaData, database, entry.getKey());
            }
        }
        return context.isUseSQLFederation();
    }
    
    private boolean isQuerySystemSchema(final SQLStatementContext<?> sqlStatementContext, final ShardingSphereDatabase database) {
        return sqlStatementContext instanceof SelectStatementContext
                && SystemSchemaUtils.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database);
    }
}
