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

import com.ecwid.consul.UrlParameters;
import com.ecwid.consul.Utils;
import com.ecwid.consul.v1.ConsistencyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ShardingConsul Query Params support wait time MILLISECONDS level.
 * @author Gavin.peng
 */
public final class ShardingSphereQueryParams implements UrlParameters {
    
    public static final ShardingSphereQueryParams DEFAULT = new ShardingSphereQueryParams(ConsistencyMode.DEFAULT);
    
    private final String datacenter;
    
    private final ConsistencyMode consistencyMode;
    
    private final long waitTime;
    
    private TimeUnit timeUnit;
    
    private final long index;
    
    private final String near;
    
    private ShardingSphereQueryParams(final String datacenter, final ConsistencyMode consistencyMode, final long waitTime, final TimeUnit timeUnit, final long index, final String near) {
        this.datacenter = datacenter;
        this.consistencyMode = consistencyMode;
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;
        this.index = index;
        this.near = near;
    }
    
    private ShardingSphereQueryParams(final String datacenter, final ConsistencyMode consistencyMode, final long waitTime, final long index) {
        this(datacenter, consistencyMode, waitTime, TimeUnit.MILLISECONDS, index, null);
    }
    
    public ShardingSphereQueryParams(final ConsistencyMode consistencyMode) {
        this(null, consistencyMode, -1, -1);
    }
    
    public ShardingSphereQueryParams(final long waitTime, final long index) {
        this(null, ConsistencyMode.DEFAULT, waitTime, index);
    }
    
    @Override
    public List<String> toUrlParameters() {
        List<String> params = new ArrayList<String>();
        if (datacenter != null) {
            params.add("dc=" + Utils.encodeValue(datacenter));
        }
        if (consistencyMode != ConsistencyMode.DEFAULT) {
            params.add(consistencyMode.name().toLowerCase());
        }
        if (waitTime != -1) {
            String waitStr = String.valueOf(timeUnit.toMillis(waitTime)) + "ms";
            params.add("wait=" + waitStr);
        }
        if (index != -1) {
            params.add("index=" + Utils.toUnsignedString(index));
        }
        if (near != null) {
            params.add("near=" + Utils.encodeValue(near));
        }
        return params;
    }
}
