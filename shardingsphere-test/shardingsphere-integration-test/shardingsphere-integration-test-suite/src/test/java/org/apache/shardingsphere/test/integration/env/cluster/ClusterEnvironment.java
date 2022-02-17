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

package org.apache.shardingsphere.test.integration.env.cluster;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Cluster environment.
 */
@Getter
public final class ClusterEnvironment {
    
    private final ClusterEnvironmentType environmentType;
    
    private final Collection<String> adapters;
    
    private final Collection<DatabaseType> databaseTypes;
    
    public ClusterEnvironment(final Properties envProps) {
        environmentType = getEnvironmentType(envProps);
        adapters = getAdapters(envProps);
        databaseTypes = getDatabaseTypes(envProps);
    }
    
    private ClusterEnvironmentType getEnvironmentType(final Properties envProps) {
        String value = envProps.getProperty("it.cluster.env.type");
        if (null == value) {
            return ClusterEnvironmentType.NATIVE;
        }
        try {
            return ClusterEnvironmentType.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return ClusterEnvironmentType.NATIVE;
        }
    }
    
    private Collection<String> getAdapters(final Properties envProps) {
        return Splitter.on(",").trimResults().splitToList(envProps.getProperty("it.cluster.adapters"));
    }
    
    private Collection<DatabaseType> getDatabaseTypes(final Properties envProps) {
        return Arrays.stream(envProps.getProperty("it.cluster.databases").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toSet());
    }
}
