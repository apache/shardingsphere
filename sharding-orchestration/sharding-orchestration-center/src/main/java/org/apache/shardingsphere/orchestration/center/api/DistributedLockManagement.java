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

package org.apache.shardingsphere.orchestration.center.api;

import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.spi.TypeBasedSPI;

/**
 * Distributed Lock center.
 *
 * @author zhangliang
 * @author sunbufu
 * @author dongzonglei
 * @author wangguangyuan
 */
public interface DistributedLockManagement extends TypeBasedSPI {
    
    /**
     * Initialize distributed lock center.
     *
     * @param config distributed lock center configuration
     */
    void init(OrchestrationConfiguration config);

    /**
     * Get data from distributed lock center.
     *
     * <p>Maybe use cache if existed.</p>
     *
     * @param key key of data
     * @return value of data
     */
    String get(String key);
    
    /**
     * Persist data.
     *
     * @param key key of data
     * @param value value of data
     */
    void persist(String key, String value);
    
    /**
     * Close.
     */
    void close();

    /**
     * Initialize the lock of the key.
     *
     * @param key key of data
     */
    void initLock(String key);

    /**
     * Try to get the lock of the key.
     *
     * @return get the lock or not
     */
    boolean tryLock();

    /**
     * Try to release the lock of the key.
     *
     */
    void tryRelease();
}
