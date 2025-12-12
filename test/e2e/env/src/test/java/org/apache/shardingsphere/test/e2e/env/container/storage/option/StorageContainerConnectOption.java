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

/**
 * Storage container connect option.
 */
public interface StorageContainerConnectOption {
    
    /**
     * Get driver class name.
     *
     * @return driver class name
     * @throws UnsupportedOperationException unsupported operation exception
     */
    String getDriverClassName();
    
    /**
     * Get URL.
     *
     * @param host database host
     * @param port database port
     * @return URL
     */
    String getURL(String host, int port);
    
    /**
     * Get URL.
     *
     * @param host database host
     * @param port database port
     * @param dataSourceName data source name
     * @return URL
     */
    String getURL(String host, int port, String dataSourceName);
}
