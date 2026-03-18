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

package org.apache.shardingsphere.proxy.frontend.netty;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChannelAttrInitializerTest {
    
    @Test
    void assertChannelActive() {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        new ChannelAttrInitializer().channelActive(context);
        verify(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).setIfAbsent(any(Charset.class));
        verify(context).fireChannelActive();
    }
}
