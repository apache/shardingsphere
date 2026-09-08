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
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableValueProvider;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset.MySQLSessionCharsetContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Optional;

/**
 * Character set value provider.
 */
public final class CharsetValueProvider implements MySQLSystemVariableValueProvider {
    
    @Override
    public String get(final MySQLSystemVariableScope scope, final ConnectionSession connectionSession, final MySQLSystemVariable variable) {
        return getOptional(scope, connectionSession, variable).orElse("NULL");
    }
    
    @Override
    public Optional<String> getOptional(final MySQLSystemVariableScope scope, final ConnectionSession connectionSession, final MySQLSystemVariable variable) {
        MySQLSessionCharsetContext context = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
        switch (variable) {
            case CHARACTER_SET_CLIENT:
                return Optional.of(context.getClientCharacterSetName());
            case CHARACTER_SET_CONNECTION:
                return Optional.of(context.getConnectionCharacterSetName());
            case CHARACTER_SET_RESULTS:
                return context.getResultCharacterSetName();
            case COLLATION_CONNECTION:
                return Optional.of(context.getConnectionCollationName());
            default:
                return Optional.of(variable.getDefaultValue());
        }
    }
}
