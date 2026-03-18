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

package org.apache.shardingsphere.sql.parser.statement.mysql.dal.plugin;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

import java.util.Collections;
import java.util.Map;

/**
 * Install plugin statement for MySQL.
 */
@Getter
public final class MySQLInstallPluginStatement extends DALStatement {
    
    private final String pluginName;
    
    private final String source;
    
    private final Map<String, String> properties;
    
    public MySQLInstallPluginStatement(final DatabaseType databaseType, final String pluginName) {
        super(databaseType);
        this.pluginName = pluginName;
        this.source = null;
        this.properties = Collections.emptyMap();
    }
    
    public MySQLInstallPluginStatement(final DatabaseType databaseType, final String source, final Map<String, String> properties) {
        super(databaseType);
        this.pluginName = null;
        this.source = source;
        this.properties = null == properties ? Collections.emptyMap() : properties;
    }
}
