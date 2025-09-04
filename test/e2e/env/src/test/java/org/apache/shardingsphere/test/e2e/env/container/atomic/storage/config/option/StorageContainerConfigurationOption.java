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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option;

import java.util.Map;

/**
 * Storage container configuration option.
 */
public interface StorageContainerConfigurationOption {
    
    /**
     * Get command.
     *
     * @return command
     */
    String getCommand();
    
    /**
     * Get container environments.
     *
     * @return container environments
     */
    Map<String, String> getContainerEnvironments();
    
    /**
     * Get mounted resources.
     *
     * @return mounted resources
     */
    Map<String, String> getMountedResources();
    
    /**
     * Get mounted resources.
     *
     * @param scenario scenario
     * @return mounted resources
     */
    Map<String, String> getMountedResources(String scenario);
    
    /**
     * Get mounted resources.
     *
     * @param majorVersion major version
     * @return mounted resources
     */
    Map<String, String> getMountedResources(int majorVersion);
    
    /**
     * Whether embedded storage container.
     *
     * @return is embedded storage container or not
     */
    boolean isEmbeddedStorageContainer();
    
    /**
     * Whether recognize major version.
     *
     * @return recognized major version or not
     */
    boolean isRecognizeMajorVersion();
}
