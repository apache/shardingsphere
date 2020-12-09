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

package org.apache.shardingsphere.infra.audit;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL audit engine.
 */
public final class SQLAuditEngine {
    
    static {
        ShardingSphereServiceLoader.register(SQLAuditor.class);
    }
    
    /**
     * Audit SQL.
     * 
     * @param sqlStatement SQL statement
     * @param parameters SQL parameters
     * @param schemaName schema name
     * @param rules ShardingSphere rules
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void audit(final SQLStatement sqlStatement, final List<Object> parameters, final String schemaName, final Collection<ShardingSphereRule> rules) throws SQLException {
        Map<ShardingSphereRule, SQLAuditor> auditors = OrderedSPIRegistry.getRegisteredServices(rules, SQLAuditor.class);
        for (Entry<ShardingSphereRule, SQLAuditor> entry : auditors.entrySet()) {
            SQLAuditResult auditResult = entry.getValue().audit(sqlStatement, parameters, schemaName, entry.getKey());
            if (!auditResult.isPassed()) {
                throw new SQLException(auditResult.getFailedReason(), AuditSQLState.COMMON_AUDIT_FAIL);
            }
        }
    }
}
