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

package org.apache.shardingsphere.database.connector.firebird.metadata.database.system;

import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;

import java.util.Collection;
import java.util.Collections;

/**
 * System database of Firebird.
 */
public final class FirebirdSystemDatabase implements DialectSystemDatabase {
    
    private static final Collection<String> SYSTEM_SCHEMAS = Collections.singleton("system_tables");
    
    @Override
    public Collection<String> getSystemDatabases() {
        return Collections.emptyList();
    }
    
    @Override
    public Collection<String> getSystemSchemas(final String databaseName) {
        return SYSTEM_SCHEMAS;
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return SYSTEM_SCHEMAS;
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
