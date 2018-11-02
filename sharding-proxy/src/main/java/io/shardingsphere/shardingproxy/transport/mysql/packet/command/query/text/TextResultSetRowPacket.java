/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text;

import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Text result set row packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-ProtocolText::ResultsetRow">ResultsetRow</a>
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class TextResultSetRowPacket implements MySQLPacket {
    
    private static final int NULL = 0xfb;
    
    private final int sequenceId;
    
    private final List<Object> data;
    
    public TextResultSetRowPacket(final MySQLPacketPayload payload, final int columnCount) {
        sequenceId = payload.readInt1();
        data = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            data.add(payload.readStringLenenc());
        }
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        for (Object each : data) {
            if (null == each) {
                payload.writeInt1(NULL);
            } else {
                payload.writeStringLenenc(each.toString());
            }
        }
    }
}
