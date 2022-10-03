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

package org.apache.shardingsphere.mode.repository.cluster.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;

/**
 * ShardingSphere consul client support use raw client.
 * @Author: Gavin.peng
 * @Date: 2022/9/25 15:41
 */
public class ShardingSphereConsulClient extends ConsulClient {
    
    private ConsulRawClient rawClient;
    
    public ShardingSphereConsulClient(final ConsulRawClient rawClient) {
        super(rawClient);
        this.rawClient = rawClient;
    }
    
    /**
     * Get consul raw client.
     * @return raw consul client
     */
    public ConsulRawClient getRawClient() {
        return rawClient;
    }
}
