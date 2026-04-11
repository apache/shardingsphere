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

package org.apache.shardingsphere.test.e2e.mcp.llm.config;

final class LLME2EConfigurationEnvProbe {
    
    /**
     * Entry point for child-process environment configuration probes.
     *
     * @param args command arguments
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        System.out.println("enabled=" + actual.enabled());
        System.out.println("baseUrl=" + actual.baseUrl());
        System.out.println("modelName=" + actual.modelName());
        System.out.println("requestTimeoutSeconds=" + actual.requestTimeoutSeconds());
        System.out.println("maxTurns=" + actual.maxTurns());
        System.out.println("artifactRoot=" + actual.artifactRoot());
        System.out.println("runId=" + actual.runId());
    }
}
