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

package org.apache.shardingsphere.test.e2e.env.runtime.cluster;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Cluster environment.
 */
@Getter
public final class ClusterEnvironment {
    
    private final Type type;
    
    private final Collection<String> adapters;
    
    private final Collection<DatabaseType> databaseTypes;
    
    public ClusterEnvironment(final Properties props) {
        type = getType(props);
        adapters = getAdapters(props);
        databaseTypes = getDatabaseTypes(props);
    }
    
    private Type getType(final Properties props) {
        String value = props.getProperty("it.cluster.env.type");
        if (null == value) {
            return Type.NATIVE;
        }
        try {
            return Type.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return Type.NATIVE;
        }
    }
    
    private Collection<String> getAdapters(final Properties props) {
        return Splitter.on(",").trimResults().splitToList(props.getProperty("it.cluster.adapters"));
    }
    
    private Collection<DatabaseType> getDatabaseTypes(final Properties props) {
        return Arrays.stream(props.getProperty("it.cluster.databases").split(",")).map(each -> TypedSPILoader.getService(DatabaseType.class, each.trim())).collect(Collectors.toSet());
    }
    
    /**
     * Cluster environment type.
     */
    public enum Type {
        
        DOCKER, NATIVE
    }
}
