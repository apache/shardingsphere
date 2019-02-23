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

package org.apache.shardingsphere.shardingproxy.backend.result.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;

import java.util.Collection;
import java.util.Collections;

/**
 * Success response.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SuccessResponse implements BackendResponse {
    
    private final long affectedRows;
    
    private final long lastInsertId;
    
    public SuccessResponse() {
        this(0L, 0L);
    }
    
    @Override
    public DatabasePacket getHeadPacket() {
        return new DatabaseSuccessPacket(1, affectedRows, lastInsertId);
    }
    
    @Override
    public Collection<DatabasePacket> getPackets() {
        return Collections.singletonList(getHeadPacket());
    }
}
