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

package org.apache.shardingsphere.authority.rule.builder;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.builder.GlobalRuleBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Authority rule builder.
 */
public final class AuthorityRuleBuilder implements GlobalRuleBuilder<AuthorityRule, AuthorityRuleConfiguration> {
    
    @Override
    public AuthorityRule build(final Map<String, ShardingSphereMetaData> mataDataMap, final AuthorityRuleConfiguration ruleConfig, final Collection<ShardingSphereUser> users) {
        DatabaseType databaseType = mataDataMap.isEmpty() ? new MySQLDatabaseType() : getDatabaseType(mataDataMap.values().iterator().next().getResource().getDataSources());
        return new AuthorityRule(ruleConfig, mataDataMap, databaseType, users);
    }
    
    private static DatabaseType getDatabaseType(final Map<String, DataSource> dataSourceMap) {
        DatabaseType result = null;
        for (DataSource each : dataSourceMap.values()) {
            DatabaseType databaseType = getDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, String.format("Database type inconsistent with '%s' and '%s'", result, databaseType));
            result = databaseType;
        }
        return null == result ? DatabaseTypeRegistry.getDefaultDatabaseType() : result;
    }

    private static DatabaseType getDatabaseType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            return null;
        }
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
    
    @Override
    public Class<AuthorityRuleConfiguration> getTypeClass() {
        return AuthorityRuleConfiguration.class;
    }
}
