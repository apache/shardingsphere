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

package org.apache.shardingsphere.spring.boot.schema;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

/**
 * Database name setter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseNameSetter {
    
    private static final String DATABASE_NAME_KEY = "spring.shardingsphere.database.name";
    
    private static final String SCHEMA_NAME_KEY = "spring.shardingsphere.schema.name";
    
    /**
     * Get database name.
     *
     * @param environment spring boot environment
     * @return schema name
     */
    public static String getDatabaseName(final Environment environment) {
        StandardEnvironment standardEnv = (StandardEnvironment) environment;
        String databaseName = standardEnv.getProperty(DATABASE_NAME_KEY);
        if (!Strings.isNullOrEmpty(databaseName)) {
            return databaseName;
        }
        String schemaName = standardEnv.getProperty(SCHEMA_NAME_KEY);
        return Strings.isNullOrEmpty(schemaName) ? DefaultDatabase.LOGIC_NAME : schemaName;
    }
}
