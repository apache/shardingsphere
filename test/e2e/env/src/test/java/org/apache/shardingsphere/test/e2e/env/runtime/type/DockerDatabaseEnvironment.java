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

package org.apache.shardingsphere.test.e2e.env.runtime.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Docker database environment.
 */
public final class DockerDatabaseEnvironment {
    
    private final Map<DatabaseType, String> images;
    
    public DockerDatabaseEnvironment(final Properties props) {
        images = getImages(props);
    }
    
    private Map<DatabaseType, String> getImages(final Properties props) {
        Collection<DatabaseType> databaseTypes = ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
        Map<DatabaseType, String> result = new HashMap<>(databaseTypes.size(), 1F);
        for (DatabaseType each : databaseTypes) {
            Optional.ofNullable(props.getProperty(String.format("e2e.docker.database.%s.image", each.getType().toLowerCase()))).ifPresent(value -> result.put(each, value));
        }
        return result;
    }
    
    /**
     * Get image.
     *
     * @param databaseType database type
     * @return database image
     */
    public String getImage(final DatabaseType databaseType) {
        return images.get(databaseType);
    }
}
