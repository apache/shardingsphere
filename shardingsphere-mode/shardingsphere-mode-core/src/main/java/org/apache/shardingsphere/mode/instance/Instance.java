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

package org.apache.shardingsphere.mode.instance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.instance.utils.IpUtils;

import java.lang.management.ManagementFactory;

/**
 * Instance.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Instance {
    
    private static final String DELIMITER = "@";
    
    private static final Instance INSTANCE = new Instance();
    
    private volatile String id;
    
    /**
     * Init instance.
     * 
     * @param port port
     */
    public synchronized void init(final Integer port) {
        id = String.join(DELIMITER, IpUtils.getIp(), null == port ? ManagementFactory.getRuntimeMXBean().getName().split(DELIMITER)[0] : String.valueOf(port));
    }
    
    /**
     * Get instance id.
     * 
     * @param ip ip
     * @param port port
     * @return instance id
     */
    public String getInstanceId(final String ip, final String port) {
        return String.join(DELIMITER, ip, port);
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static Instance getInstance() {
        return INSTANCE;
    }
}
