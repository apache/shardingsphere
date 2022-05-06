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

package org.apache.shardingsphere.infra.instance.definition;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Instance id.
 */
@Getter
public final class InstanceId {
    
    private static final String DELIMITER = "@";
    
    private static final AtomicLong ATOMIC_LONG = new AtomicLong();
    
    private final String id;
    
    private final String ip;
    
    private final Integer uniqueSign;
    
    public InstanceId(final String ip, final Integer uniqueSign) {
        this.ip = ip;
        this.uniqueSign = uniqueSign;
        id = String.join(DELIMITER, ip, String.valueOf(uniqueSign));
    }
    
    public InstanceId(final Integer uniqueSign) {
        this.uniqueSign = uniqueSign;
        ip = IpUtils.getIp();
        id = String.join(DELIMITER, ip, String.valueOf(uniqueSign));
    }
    
    public InstanceId(final String id) {
        this.id = id;
        List<String> ids = Splitter.on("@").splitToList(id);
        ip = ids.get(0);
        uniqueSign = Integer.valueOf(ids.get(1));
    }
    
    public InstanceId() {
        ip = IpUtils.getIp();
        uniqueSign = Integer.valueOf(String.join("", ManagementFactory.getRuntimeMXBean().getName().split(DELIMITER)[0], String.valueOf(ATOMIC_LONG.incrementAndGet())));
        id = String.join(DELIMITER, ip, String.valueOf(uniqueSign));
    }
}
