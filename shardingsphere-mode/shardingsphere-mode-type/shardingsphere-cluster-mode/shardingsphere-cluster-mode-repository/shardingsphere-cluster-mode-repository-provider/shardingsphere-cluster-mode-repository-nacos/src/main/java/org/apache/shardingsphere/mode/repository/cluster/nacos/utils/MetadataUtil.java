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

package org.apache.shardingsphere.mode.repository.cluster.nacos.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.SneakyThrows;

import java.time.ZoneOffset;

/**
 * Metadata util.
 */
public class MetadataUtil {
    
    /**
     * Get timestamp.
     * @param instance instance
     * @return timestamp
     */
    @SneakyThrows
    public static long getTimestamp(final Instance instance) {
        return Long.parseLong(instance.getMetadata().get(ZoneOffset.of("+8").toString()));
    }
    
    /**
     * Get value.
     * @param instance instance
     * @return value
     */
    public static String getValue(final Instance instance) {
        return instance.getMetadata().get(getKey(instance));
    }
    
    /**
     * Get key.
     * @param instance instance
     * @return key
     */
    @SneakyThrows
    public static String getKey(final Instance instance) {
        return instance.getMetadata().keySet().stream()
                .filter(entryKey -> !entryKey.equals(PreservedMetadataKeys.HEART_BEAT_INTERVAL)
                        && !entryKey.equals(PreservedMetadataKeys.HEART_BEAT_TIMEOUT)
                        && !entryKey.equals(PreservedMetadataKeys.IP_DELETE_TIMEOUT)
                        && !entryKey.equals(ZoneOffset.of("+8").toString()))
                .findFirst().orElseThrow(() -> new NacosException(NacosException.RESOURCE_NOT_FOUND, "Failed to find key "));
    }
}
