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

package org.apache.shardingsphere.proxy.frontend.mysql.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;

import java.util.List;

/**
 * MySQL SSL request handler.
 */
public final class MySQLSSLRequestHandler extends ByteToMessageDecoder {
    
    private static final int HEADER_LENGTH = 4;
    
    private static final int SSL_REQUEST_LENGTH = 32;
    
    public MySQLSSLRequestHandler() {
        setSingleDecode(true);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        if (in.readableBytes() < HEADER_LENGTH || in.readableBytes() < HEADER_LENGTH + in.getUnsignedMediumLE(in.readerIndex())) {
            return;
        }
        if (isSSLRequest(in)) {
            SslHandler sslHandler = new SslHandler(ProxySSLContext.getInstance().newSSLEngine(context.alloc()));
            context.pipeline().addAfter(MySQLSSLRequestHandler.class.getSimpleName(), SslHandler.class.getSimpleName(), sslHandler);
            in.skipBytes(HEADER_LENGTH + SSL_REQUEST_LENGTH);
        }
        context.pipeline().remove(this);
    }
    
    private boolean isSSLRequest(final ByteBuf in) {
        int clientCapabilitiesFlagOffset = HEADER_LENGTH + in.readerIndex();
        return SSL_REQUEST_LENGTH == in.getUnsignedMediumLE(in.readerIndex())
                && MySQLCapabilityFlag.CLIENT_SSL.getValue() == (MySQLCapabilityFlag.CLIENT_SSL.getValue() & in.getUnsignedShortLE(clientCapabilitiesFlagOffset));
    }
}
