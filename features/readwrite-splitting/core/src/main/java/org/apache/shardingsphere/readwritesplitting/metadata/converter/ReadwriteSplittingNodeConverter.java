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

package org.apache.shardingsphere.readwritesplitting.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Readwrite-splitting node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingNodeConverter {
    
    private static final String ROOT_NODE = "readwrite_splitting";
    
    private static final String DATA_SOURCES_NODE = "data_sources";
    
    private static final String LOAD_BALANCER_NODE = "load_balancers";
    
    private static final String RULES_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/";
    
    /**
     * Get group name path.
     *
     * @param groupName group name
     * @return group name path
     */
    public static String getGroupNamePath(final String groupName) {
        return String.join("/", DATA_SOURCES_NODE, groupName);
    }
    
    /**
     * Get load balancer name.
     *
     * @param loadBalancerName load balancer name
     * @return load balancer path
     */
    public static String getLoadBalancerPath(final String loadBalancerName) {
        return String.join("/", LOAD_BALANCER_NODE, loadBalancerName);
    }
    
    /**
     * Is readwrite-splitting path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isReadwriteSplittingPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Get group name.
     *
     * @param rulePath rule path
     * @return group name
     */
    public static Optional<String> getGroupName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DATA_SOURCES_NODE + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     *  Get group name.
     *
     * @param rulePath rule path
     * @return group name
     */
    public static Optional<String> getLoadBalanceName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + LOAD_BALANCER_NODE + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
