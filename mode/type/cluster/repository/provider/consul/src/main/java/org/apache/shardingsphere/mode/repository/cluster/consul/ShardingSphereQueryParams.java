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
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ShardingSphere query params.
 */
@RequiredArgsConstructor
public final class ShardingSphereQueryParams implements UrlParameters {
    
    private final long waitMillis;
    
    private final long index;
    
    @Override
    public List<String> toUrlParameters() {
        List<String> result = new ArrayList<>(2);
        if (-1 != waitMillis) {
            result.add(String.format("wait=%dms", TimeUnit.MILLISECONDS.toMillis(waitMillis)));
        }
        if (-1 != index) {
            result.add(String.format("index=%s", Utils.toUnsignedString(index)));
        }
        return result;
    }
}
