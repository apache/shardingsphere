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

package org.apache.shardingsphere.dbdiscovery.distsql.parser.segment;

import lombok.Getter;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;

import java.util.Collection;
import java.util.Properties;

/**
 * Database discovery rule definition segment.
 */
@Getter
public final class DatabaseDiscoveryDefinitionSegment extends AbstractDatabaseDiscoverySegment {
    
    private final AlgorithmSegment discoveryType;
    
    private final Properties discoveryHeartbeat;
    
    public DatabaseDiscoveryDefinitionSegment(final String name, final Collection<String> dataSources, final AlgorithmSegment discoveryType, final Properties discoveryHeartbeat) {
        super(name, dataSources);
        this.discoveryType = discoveryType;
        this.discoveryHeartbeat = discoveryHeartbeat;
    }
}
