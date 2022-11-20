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

package org.apache.shardingsphere.test.integration.discovery.util;

import lombok.NoArgsConstructor;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Discovery container utility.
 */
@NoArgsConstructor
public final class DiscoveryContainerUtil {
    
    /**
     * Get container names and quantity.
     * @param scenario scenario
     * @return container names and quantity
     */
    public static Map<String, Integer> loadContainerRawNamesAndQuantity(final String scenario) {
        Map<String, Integer> result = new HashMap<>(3, 1);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("env/scenario/" + scenario);
        if (null != resource) {
            String[] containerNames = new File(resource.getPath()).list((dir, name) -> new File(dir, name).isDirectory());
            if (null != containerNames) {
                result = extractContainerNamesWithQuantity(containerNames);
            }
        }
        return result;
    }
    
    private static Map<String, Integer> extractContainerNamesWithQuantity(final String[] rawContainerNames) {
        Map<String, Integer> result = new HashMap<>(3, 1);
        for (String each : rawContainerNames) {
            String databaseTypeName = each.contains("_") ? each.substring(0, each.indexOf("_")) : each;
            result.merge(databaseTypeName, 1, Integer::sum);
        }
        return result;
    }
}
