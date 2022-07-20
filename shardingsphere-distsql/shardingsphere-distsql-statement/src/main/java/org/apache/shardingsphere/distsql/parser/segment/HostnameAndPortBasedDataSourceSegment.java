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

package org.apache.shardingsphere.distsql.parser.segment;

import lombok.Getter;

import java.util.Properties;

/**
 * Hostname and port based data source segment.
 */
@Getter
public final class HostnameAndPortBasedDataSourceSegment extends DataSourceSegment {
    
    private final String hostname;
    
    private final String port;
    
    private final String database;
    
    public HostnameAndPortBasedDataSourceSegment(final String name, final String hostname, final String port, final String database, final String user, final String password, final Properties props) {
        super(name, user, password, props);
        this.hostname = hostname;
        this.port = port;
        this.database = database;
    }
}
