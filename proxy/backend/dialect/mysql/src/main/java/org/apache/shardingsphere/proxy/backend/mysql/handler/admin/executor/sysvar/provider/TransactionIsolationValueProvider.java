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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider;

import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableValueProvider;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;

/**
 * Transaction isolation value provider.
 */
public final class TransactionIsolationValueProvider implements MySQLSystemVariableValueProvider {
    
    @Override
    public String get(final MySQLSystemVariableScope scope, final ConnectionSession connectionSession, final MySQLSystemVariable variable) {
        return MySQLSystemVariableScope.GLOBAL == scope ? variable.getDefaultValue() : connectionSession.getIsolationLevel().orElse(TransactionIsolationLevel.REPEATABLE_READ).getIsolationLevel();
    }
}
