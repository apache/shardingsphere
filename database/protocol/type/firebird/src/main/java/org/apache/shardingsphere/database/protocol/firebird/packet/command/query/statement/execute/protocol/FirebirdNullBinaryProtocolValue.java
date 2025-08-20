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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * NULL protocol value for string for Firebird.
 */
public final class FirebirdNullBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        return null;
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        payload.writeBuffer(new byte[0]);
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 0;
    }
}
