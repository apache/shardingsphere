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

package io.shardingsphere.proxy;

import io.shardingsphere.proxy.transport.mysql.packet.handshake.AuthPluginDataTest;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.AuthorityHandlerTest;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.ConnectionIdGeneratorTest;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakePacketTest;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakeResponse41PacketTest;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.RandomGeneratorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AuthorityHandlerTest.class,
        AuthPluginDataTest.class,
        ConnectionIdGeneratorTest.class,
        HandshakePacketTest.class,
        HandshakeResponse41PacketTest.class,
        RandomGeneratorTest.class
})
public final class AllTests {
}
