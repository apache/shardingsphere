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

package org.apache.shardingsphere.test.integration.util;

public final class NetworkAliasUtil {
    
    /**
     * Get network alias with scenario.
     *
     * @param containerType container type, such as "zk", "db"
     * @param scenario scenario
     * @return network alias
     */
    public static String getNetworkAliasWithScenario(final String containerType, final String scenario) {
        return String.join(".", containerType.toLowerCase(), scenario, "host");
    }
    
    /**
     * Get network alias.
     *
     * @param containerType container type
     * @return network alias
     */
    public static String getNetworkAlias(final String containerType) {
        return String.join(".", containerType.toLowerCase(), "host");
    }
}
