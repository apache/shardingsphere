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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Dummy response packet for Firebird.
 *
 * <p>The server occasionally sends a dummy response as keep-alive signal, governed by the DummyPacketInterval server setting
 * and the isc_dpb_dummy_packet_interval connection setting. It consists of the operation code only, and is read and ignored by the client.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-responses-dummy">Firebird wire protocol - dummy response</a>
 */
public final class FirebirdDummyResponsePacket extends FirebirdPacket {
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.DUMMY.getValue());
    }
}
