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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract packet.
 *
 * <p>
 *     Implement fromByteBuf and toByteBuf in here is a bad design.
 *     But we no need to implement both of method in this project.
 * </p>
 */
@Setter
@Getter
public abstract class AbstractPacket implements Packet {
    
    private byte sequenceNumber;
    
    /**
     * empty implement method,throw {@code UnsupportedOperationException}.
     *
     * @param data buffer
     */
    @Override
    public void fromByteBuf(final ByteBuf data) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * empty implement method,throw {@code UnsupportedOperationException}.
     *
     * @return data buffer
     */
    @Override
    public ByteBuf toByteBuf() {
        throw new UnsupportedOperationException();
    }
}
