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

package org.apache.shardingsphere.replica.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

import java.util.Collection;

/**
 * Replica data source configuration.
 */
@Getter
public final class ReplicaDataSourceConfiguration implements RuleConfiguration {
    
    private final String name;
    
    private final Collection<String> replicaSourceNames;
    
    public ReplicaDataSourceConfiguration(final String name, final Collection<String> replicaSourceNames) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Name is required.");
        Preconditions.checkArgument(null != replicaSourceNames && !replicaSourceNames.isEmpty(), "replica source names are required.");
        this.name = name;
        this.replicaSourceNames = replicaSourceNames;
    }
}
