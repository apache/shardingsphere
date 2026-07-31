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

package org.apache.shardingsphere.test.e2e.mcp.env;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import java.util.Locale;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPE2ECondition {
    
    private static final Properties PROPS = EnvironmentPropertiesLoader.loadProperties();
    
    /**
     * Check whether Docker run type is enabled.
     *
     * @return whether Docker run type is enabled
     * @throws IllegalStateException when run type is unsupported
     */
    public static boolean isDockerEnabled() {
        boolean result = false;
        for (String each : EnvironmentPropertiesLoader.getListValue(PROPS, "e2e.run.type")) {
            String runType = each.toUpperCase(Locale.ENGLISH);
            if (!"DOCKER".equals(runType) && !"NATIVE".equals(runType)) {
                throw new IllegalStateException(String.format("Unsupported MCP E2E run type `%s`.", each));
            }
            if ("DOCKER".equals(runType)) {
                result = true;
            }
        }
        return result;
    }
}
