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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.proxy.frontend.context.FrontendContext;

/**
 * {@link FrontendContext} implementations for MySQL.
 */
public final class MySQLFrontendContext implements FrontendContext {
    
    private boolean previousCommandRequiresNoServerResponse;
    
    @Override
    public boolean isRequiredSameThreadForConnection(final Object message) {
        ByteBuf byteBuf = (ByteBuf) message;
        if (byteBuf.readableBytes() < 2) {
            return false;
        }
        int commandType = byteBuf.getUnsignedByte(byteBuf.readerIndex() + 1);
        boolean result = previousCommandRequiresNoServerResponse;
        previousCommandRequiresNoServerResponse = MySQLCommandPacketType.COM_STMT_CLOSE.getValue() == commandType || MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA.getValue() == commandType;
        return previousCommandRequiresNoServerResponse || result;
    }
}
