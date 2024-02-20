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

package org.apache.shardingsphere.infra.util.yaml;

/**
 * YAML configuration.
 */
public interface YamlConfiguration {
    
    /**
     * Check if the YAML configuration is empty, indicating the absence of any valid configuration items.
     * 
     * @return Judge whether the YAML configuration is empty or not
     * @throws UnsupportedOperationException unsupported operation exception
     */
    default boolean isEmpty() {
        // TODO Currently, only global.yaml and database.yaml handle the case of empty YAML file content. 
        // TODO In the future, other scenarios that read YAML files should also consider whether to override this method to determine if the YAML file is empty.
        return false;
    }
}
