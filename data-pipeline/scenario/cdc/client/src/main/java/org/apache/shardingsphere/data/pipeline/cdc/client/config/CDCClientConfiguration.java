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

package org.apache.shardingsphere.data.pipeline.cdc.client.config;

import com.google.common.base.Preconditions;
import lombok.Getter;

/**
 * CDC client configuration.
 */
@Getter
public final class CDCClientConfiguration {
    
    private final String address;
    
    private final int port;
    
    private final int timeoutMillis;
    
    public CDCClientConfiguration(final String address, final int port, final int timeoutMillis) {
        Preconditions.checkArgument(null != address && !address.isEmpty(), "The address parameter can't be null.");
        Preconditions.checkArgument(port > 0, "The port must be greater than 0.");
        this.address = address;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
    }
}
