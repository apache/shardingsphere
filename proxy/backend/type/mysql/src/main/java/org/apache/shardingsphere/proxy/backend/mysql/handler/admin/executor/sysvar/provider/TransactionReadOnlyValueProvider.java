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

import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.Scope;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableValueProvider;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * Transaction read only.
 */
public final class TransactionReadOnlyValueProvider implements MySQLSystemVariableValueProvider {
    
    @Override
    public String get(final Scope scope, final ConnectionSession connectionSession, final MySQLSystemVariable variable) {
        if (Scope.GLOBAL == scope) {
            return variable.getDefaultValue();
        }
        return connectionSession.isReadOnly() ? "1" : "0";
    }
}
