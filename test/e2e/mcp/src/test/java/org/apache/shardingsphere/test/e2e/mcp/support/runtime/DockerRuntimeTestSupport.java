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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testcontainers.DockerClientFactory;

import java.util.Optional;

/**
 * Docker runtime test support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerRuntimeTestSupport {
    
    /**
     * Require Docker to be available for a Testcontainers-backed test.
     *
     * @param scenarioMessage scenario-specific requirement message
     * @throws IllegalStateException when Docker is unavailable
     */
    public static void requireAvailable(final String scenarioMessage) {
        Optional<String> unavailableReason = getUnavailableReason();
        if (unavailableReason.isPresent()) {
            throw new IllegalStateException(scenarioMessage + " Docker readiness diagnostic: " + unavailableReason.get());
        }
    }
    
    private static Optional<String> getUnavailableReason() {
        try {
            return DockerClientFactory.instance().isDockerAvailable()
                    ? Optional.empty()
                    : Optional.of("Testcontainers Docker client reported Docker unavailable.");
        } catch (final IllegalStateException ex) {
            return Optional.of(null == ex.getMessage() || ex.getMessage().isBlank()
                    ? "Testcontainers Docker availability check failed without a message."
                    : ex.getMessage());
        }
    }
}
