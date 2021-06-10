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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Zipkin constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipkinConstants {
    
    public static final String COMPONENT_NAME = "shardingsphere";
    
    public static final String ROOT_SPAN = "zipkin_root_span";
    
    public static final String DB_TYPE_VALUE = "shardingsphere-proxy";
    
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Tags {
        
        /**
         * The tag to record the bind variables of SQL.
         */
        public static final String DB_TYPE = "db.type";
        
        public static final String DB_STATEMENT = "db.statement";
        
        public static final String DB_INSTANCE = "db.instance";
        
        public static final String DB_BIND_VARIABLES = "db.bind_vars";
        
        public static final String COMPONENT = "component";
        
        public static final String PEER_HOSTNAME = "peer.hostname";
        
        /**
         * PEER_PORT records the port number of the peer.
         */
        public static final String PEER_PORT = "peer.port";
        
        /**
         * The tag to record the connection count.
         */
        public static final String CONNECTION_COUNT = "connection.count";
    }
}
