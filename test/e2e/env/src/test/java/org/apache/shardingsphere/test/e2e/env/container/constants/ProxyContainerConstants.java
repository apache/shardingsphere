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

package org.apache.shardingsphere.test.e2e.env.container.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Proxy container constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyContainerConstants {
    
    public static final String USER = "proxy";
    
    public static final String PASSWORD = "Proxy@123";
    
    public static final String PROXY_ROOT_PATH_IN_CONTAINER = "/opt/shardingsphere-proxy/";
    
    public static final String CONFIG_PATH_IN_CONTAINER = PROXY_ROOT_PATH_IN_CONTAINER + "conf/";
    
    public static final String AGENT_CONFIG_PATH_IN_CONTAINER = PROXY_ROOT_PATH_IN_CONTAINER + "agent/conf/";
    
    public static final String PROXY_CONTAINER_NAME_PREFIX = "ShardingSphere-Proxy";
    
    public static final String PROXY_CONTAINER_IMAGE = "apache/shardingsphere-proxy-test";
}
