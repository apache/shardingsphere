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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar;

import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Optional;

/**
 * System variable value provider for MySQL.
 */
public interface MySQLSystemVariableValueProvider {
    
    MySQLSystemVariableValueProvider DEFAULT_PROVIDER = new MySQLSystemVariableValueProvider() {
    };
    
    /**
     * Get variable.
     *
     * @param scope scope
     * @param connectionSession connection session
     * @param variable variable
     * @return value of variable
     */
    default String get(final MySQLSystemVariableScope scope, final ConnectionSession connectionSession, final MySQLSystemVariable variable) {
        return variable.getDefaultValue();
    }
    
    /**
     * Get optional variable value.
     *
     * @param scope variable scope
     * @param connectionSession connection session
     * @param variable system variable
     * @return optional variable value
     */
    default Optional<String> getOptional(final MySQLSystemVariableScope scope, final ConnectionSession connectionSession, final MySQLSystemVariable variable) {
        return Optional.of(get(scope, connectionSession, variable));
    }
}
