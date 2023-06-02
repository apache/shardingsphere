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

package org.apache.shardingsphere.metadata.persist.node.metadata.config.readwritesplitting;

/**
 * Readwrite-splitting node converter.
 */
public final class ReadwriteSplittingNodeConverter {
    
    private static final String DATA_SOURCES = "data_sources";
    
    private static final String LOAD_BALANCER_NODE = "load_balancers";
    
    /**
     * Get group name path.
     * 
     * @param groupName group name
     * @return group name path
     */
    public String getGroupNamePath(final String groupName) {
        return String.join("/", DATA_SOURCES, groupName);
    }
    
    /**
     * Get load balancer name.
     * 
     * @param loadBalancerName load balancer name
     * @return load balancer path
     */
    public String getLoadBalancerPath(final String loadBalancerName) {
        return String.join("/", LOAD_BALANCER_NODE, loadBalancerName);
    }
}
