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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.process.event.KillProcessRequestEvent;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLKillStatement;

/**
 * Kill process executor.
 */
@RequiredArgsConstructor
public final class KillProcessExecutor implements DatabaseAdminExecutor {
    
    private final MySQLKillStatement killStatement;
    
    /**
     * Execute.
     *
     * @param connectionSession connection session
     */
    @Override
    public void execute(final ConnectionSession connectionSession) {
        String processId = killStatement.getProcessId();
        ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().post(new KillProcessRequestEvent(processId));
    }
}
