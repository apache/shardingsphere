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

package org.apache.shardingsphere.test.e2e.env.container.storage.option;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container create option.
 */
public interface StorageContainerCreateOption {
    
    /**
     * Get container port.
     *
     * @return container port
     */
    int getPort();
    
    /**
     * Get default container image name.
     *
     * @return default container image name
     */
    String getDefaultImageName();
    
    /**
     * Get container command.
     *
     * @return container command
     */
    String getCommand();
    
    /**
     * Get container environments.
     *
     * @return container environments
     */
    Map<String, String> getEnvironments();
    
    /**
     * Get mounted configuration resources.
     *
     * @return mounted configuration resources
     */
    Collection<String> getMountedConfigurationResources();
    
    /**
     * Get additional env mounted SQL resources.
     *
     * @param majorVersion major version
     * @return additional env mounted SQL resources
     */
    Collection<String> getAdditionalEnvMountedSQLResources(int majorVersion);
    
    /**
     * Get supported major versions.
     *
     * @return supported major versions
     */
    List<Integer> getSupportedMajorVersions();
    
    /**
     * Whether with privileged mode.
     *
     * @return is with privileged mode or not
     */
    boolean withPrivilegedMode();
    
    /**
     * Get the default database name.
     *
     * @param majorVersion major version
     * @return default database name
     */
    Optional<String> getDefaultDatabaseName(int majorVersion);
    
    /**
     * Get container startup timeout seconds.
     *
     * @return container startup timeout seconds
     */
    long getStartupTimeoutSeconds();
    
    /**
     * Whether support docker entrypoint.
     *
     * @return is support docker entrypoint or not
     */
    default boolean isSupportDockerEntrypoint() {
        return true;
    }
    
    /**
     * Get default user when unsupported docker entrypoint.
     *
     * @return default user
     */
    default Optional<String> getDefaultUserWhenUnsupportedDockerEntrypoint() {
        return Optional.empty();
    }
    
    /**
     * Get default password when unsupported docker entrypoint.
     *
     * @return default password
     */
    default Optional<String> getDefaultPasswordWhenUnsupportedDockerEntrypoint() {
        return Optional.empty();
    }
}
