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
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * COM_INIT_DB command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComInitDbExecutor implements CommandExecutor {
    
    private final MySQLComInitDbPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        String databaseName = SQLUtil.getExactlyValue(packet.getSchema());
        if (ProxyContext.getInstance().databaseExists(databaseName) && SQLCheckEngine.check(databaseName, getRules(databaseName), connectionSession.getGrantee())) {
            connectionSession.setCurrentDatabase(packet.getSchema());
            return Collections.singletonList(new MySQLOKPacket(1, ServerStatusFlagCalculator.calculateFor(connectionSession)));
        }
        throw new UnknownDatabaseException(packet.getSchema());
    }
    
    private Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
}
