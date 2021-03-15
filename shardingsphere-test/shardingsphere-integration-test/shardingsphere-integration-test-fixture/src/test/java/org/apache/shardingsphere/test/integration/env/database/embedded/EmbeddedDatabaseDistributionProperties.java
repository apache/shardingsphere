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

package org.apache.shardingsphere.test.integration.env.database.embedded;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.Properties;

/**
 * Embedded database distribution properties.
 */
@RequiredArgsConstructor
public final class EmbeddedDatabaseDistributionProperties {
    
    private final Properties props;
    
    /**
     * Get database distribution URL.
     *
     * @param databaseType database type
     * @return database distribution URL
     */
    public String getURL(final DatabaseType databaseType) {
        return props.getProperty(String.format("it.%s.distribution.url", databaseType.getName().toLowerCase()));
    }
    
    /**
     * Get database distribution version.
     *
     * @param databaseType database type
     * @return database distribution version
     */
    public String getVersion(final DatabaseType databaseType) {
        return props.getProperty(String.format("it.%s.distribution.version", databaseType.getName().toLowerCase()));
    }
    
    /**
     * Get database port.
     *
     * @param databaseType database type
     * @return database port
     */
    public int getInstancePort(final DatabaseType databaseType) {
        return Integer.parseInt(props.getProperty(String.format("it.%s.instance.port", databaseType.getName().toLowerCase())));
    }
}
