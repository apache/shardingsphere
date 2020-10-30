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

package org.apache.shardingsphere.infra.parser.hook;

import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ServiceLoader;

/**
 * Parsing hook registry.
 */
public final class ParsingHookRegistry {
    
    private static final ParsingHookRegistry INSTANCE = new ParsingHookRegistry();
    
    private final Collection<ParsingHook> hooks = new LinkedList<>();
    
    private ParsingHookRegistry() {
        for (ParsingHook each : ServiceLoader.load(ParsingHook.class)) {
            hooks.add(each);
        }
    }
    
    /**
     * Get instance.
     * 
     * @return instance
     */
    public static ParsingHookRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Handle when parse started.
     *
     * @param sql SQL to be parsed
     */
    public void start(final String sql) {
        hooks.forEach(each -> each.start(sql));
    }
    
    /**
     * Handle when parse finished success.
     *
     * @param sqlStatement sql statement
     */
    public void finishSuccess(final SQLStatement sqlStatement) {
        hooks.forEach(each -> each.finishSuccess(sqlStatement));
    }
    
    /**
     * Handle when parse finished failure.
     *
     * @param cause failure cause
     */
    public void finishFailure(final Exception cause) {
        hooks.forEach(each -> each.finishFailure(cause));
    }
}
