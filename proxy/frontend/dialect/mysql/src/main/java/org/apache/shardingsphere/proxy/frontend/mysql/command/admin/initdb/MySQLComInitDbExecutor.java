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

package org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * COM_INIT_DB command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComInitDbExecutor implements CommandExecutor {
    
    private final MySQLComInitDbPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        String databaseName = SQLUtils.getExactlyValue(packet.getSchema());
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        AuthorityChecker authorityChecker = new AuthorityChecker(authorityRule, connectionSession.getConnectionContext().getGrantee());
        ShardingSpherePreconditions.checkState(
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().containsDatabase(databaseName) && authorityChecker.isAuthorized(databaseName),
                () -> new UnknownDatabaseException(packet.getSchema()));
        connectionSession.setCurrentDatabaseName(packet.getSchema());
        return Collections.singleton(new MySQLOKPacket(ServerStatusFlagCalculator.calculateFor(connectionSession, true)));
    }
}
