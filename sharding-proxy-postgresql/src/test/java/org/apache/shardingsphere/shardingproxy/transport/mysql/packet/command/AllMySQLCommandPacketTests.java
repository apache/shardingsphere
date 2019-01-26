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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command;

import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.UnsupportedCommandPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.ComInitDbPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.ping.ComPingPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit.ComQuitPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41PacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryResponsePacketsTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistryTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.ComStmtClosePacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.BinaryResultSetRowPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.ComStmtExecutePacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.NullBitmapTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.AllMySQLBinaryProtocolTests;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.ComStmtPrepareOKPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.ComStmtPreparePacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.TextResultSetRowPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.ComFieldListPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.ComQueryPacketTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        CommandPacketTypeTest.class, 
        CommandPacketFactoryTest.class, 
        CommandResponsePacketsTest.class, 
        QueryResponsePacketsTest.class, 
        BinaryStatementRegistryTest.class, 
        NullBitmapTest.class,
        AllMySQLBinaryProtocolTests.class, 
        FieldCountPacketTest.class, 
        ColumnDefinition41PacketTest.class, 
        TextResultSetRowPacketTest.class, 
        ComFieldListPacketTest.class, 
        ComQueryPacketTest.class, 
        ComStmtPreparePacketTest.class, 
        ComStmtPrepareOKPacketTest.class, 
        BinaryResultSetRowPacketTest.class, 
        ComStmtExecutePacketTest.class, 
        ComStmtClosePacketTest.class, 
        ComInitDbPacketTest.class, 
        ComPingPacketTest.class, 
        ComQuitPacketTest.class, 
        UnsupportedCommandPacketTest.class
})
public final class AllMySQLCommandPacketTests {
}
