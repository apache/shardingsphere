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

package org.apache.shardingsphere.data.pipeline.cdc.client.context;

import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.cdc.client.constant.ClientConnectionStatus;

/**
 * Client connection context.
 */
@Getter
@Setter
public final class ClientConnectionContext {
    
    public static final AttributeKey<ClientConnectionContext> CONTEXT_KEY = AttributeKey.valueOf("client.context");
    
    private volatile ClientConnectionStatus status;
    
    private volatile String streamingId;
}
