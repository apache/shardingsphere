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

package org.apache.shardingsphere.infra.binder.decider.engine;

import org.apache.shardingsphere.infra.binder.decider.SQLFederationDecider;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
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
 * SQL federation decider engine.
 */
public final class SQLFederationDeciderEngine {
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLFederationDecider> deciders;
    
    public SQLFederationDeciderEngine(final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) {
        this.props = props;
        deciders = OrderedSPILoader.getServices(SQLFederationDecider.class, rules);
    }
    
    /**
     * Decide.
     * 
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param globalRuleMetaData global rule meta data
     * @param database ShardingSphere database
     * @return SQL federation decider context
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SQLFederationDeciderContext decide(final SQLStatementContext<?> sqlStatementContext,
                                              final List<Object> parameters, final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database) {
        SQLFederationDeciderContext result = new SQLFederationDeciderContext();
        // TODO move this logic to SQLFederationDecider implement class when we remove sql federation type
        if (isSelectStatementContainsSystemSchema(sqlStatementContext, database)) {
            result.setUseSQLFederation(true);
            return result;
        }
        String sqlFederationType = props.getValue(ConfigurationPropertyKey.SQL_FEDERATION_TYPE);
        if ("NONE".equals(sqlFederationType) || !(sqlStatementContext instanceof SelectStatementContext)) {
            return result;
        }
        for (Entry<ShardingSphereRule, SQLFederationDecider> entry : deciders.entrySet()) {
            if (!result.isUseSQLFederation()) {
                entry.getValue().decide(result, sqlStatementContext, parameters, globalRuleMetaData, database, entry.getKey(), props);
            }
        }
        return result;
    }
    
    private boolean isSelectStatementContainsSystemSchema(final SQLStatementContext<?> sqlStatementContext, final ShardingSphereDatabase database) {
        return sqlStatementContext instanceof SelectStatementContext
                && SystemSchemaUtils.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database);
    }
}
