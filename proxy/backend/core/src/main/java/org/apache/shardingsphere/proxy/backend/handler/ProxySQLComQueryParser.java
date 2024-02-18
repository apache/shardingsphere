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

package org.apache.shardingsphere.proxy.backend.handler;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

/**
 * Proxy SQL com query parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxySQLComQueryParser {
    
    /**
     * Parse SQL.
     * 
     * @param sql SQL to be parsed
     * @param databaseType database type
     * @param connectionSession connection session
     * @return SQL statement
     */
    public static SQLStatement parse(final String sql, final DatabaseType databaseType, final ConnectionSession connectionSession) {
        if (SQLUtils.trimComment(sql).isEmpty()) {
            return new EmptyStatement();
        }
        SQLParserRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        return rule.getSQLParserEngine(getProtocolType(databaseType, connectionSession)).parse(sql, false);
    }
    
    private static DatabaseType getProtocolType(final DatabaseType defaultDatabaseType, final ConnectionSession connectionSession) {
        String databaseName = connectionSession.getDatabaseName();
        return Strings.isNullOrEmpty(databaseName) || !ProxyContext.getInstance().databaseExists(databaseName)
                ? defaultDatabaseType
                : ProxyContext.getInstance().getContextManager().getDatabase(databaseName).getProtocolType();
    }
}
