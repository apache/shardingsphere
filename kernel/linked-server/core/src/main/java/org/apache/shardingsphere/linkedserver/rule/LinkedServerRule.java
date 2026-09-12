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

package org.apache.shardingsphere.linkedserver.rule;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.linkedserver.config.LinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.config.rule.LinkedServerConfiguration;
import org.apache.shardingsphere.linkedserver.constant.LinkedServerOrder;
import org.apache.shardingsphere.linkedserver.rule.table.LinkedServerTable;

import java.util.Map;
import java.util.Optional;

/**
 * Linked server rule.
 */
public final class LinkedServerRule implements DatabaseRule {
    
    private final LinkedServerRuleConfiguration configuration;
    
    private final Map<String, LinkedServerTable> servers;
    
    public LinkedServerRule(final LinkedServerRuleConfiguration configuration) {
        this.configuration = configuration;
        servers = new CaseInsensitiveMap<>();
        for (LinkedServerConfiguration each : configuration.getServers()) {
            servers.put(each.getName(), new LinkedServerTable(each));
        }
    }
    
    /**
     * Find logical table name by linked server name and remote table name.
     *
     * @param serverName linked server name
     * @param remoteTableName remote table name
     * @return logical table name
     */
    public Optional<String> findLogicalTable(final String serverName, final String remoteTableName) {
        LinkedServerTable serverTable = servers.get(serverName);
        if (null == serverTable) {
            return Optional.empty();
        }
        return serverTable.findLogicalTable(remoteTableName);
    }
    
    /**
     * Find database type for linked server.
     *
     * @param serverName linked server name
     * @return database type
     */
    public Optional<DatabaseType> findDatabaseType(final String serverName) {
        LinkedServerTable serverTable = servers.get(serverName);
        return null == serverTable ? Optional.empty() : Optional.of(serverTable.getDatabaseType());
    }
    
    /**
     * Check if linked server exists.
     *
     * @param serverName linked server name
     * @return true if exists
     */
    public boolean containsServer(final String serverName) {
        return servers.containsKey(serverName);
    }
    
    @Override
    public LinkedServerRuleConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    public int getOrder() {
        return LinkedServerOrder.ORDER;
    }
}
