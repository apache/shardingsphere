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

import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.MySQLUnsupportedCommandPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.MySQLComInitDbPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.ping.MySQLComPingPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit.MySQLComQuitPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41PacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLFieldCountPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLMySQLBinaryStatementRegistryTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.MySQLComStmtClosePacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLBinaryResultSetRowPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLNullBitmapTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.AllMySQLBinaryProtocolTests;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareMySQLOKPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.MySQLTextResultSetRowPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacketTest;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.MySQLComQueryPacketTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        MySQLMySQLCommandPacketTypeTest.class,
        MySQLMySQLCommandPacketFactoryTest.class,
        MySQLMySQLBinaryStatementRegistryTest.class,
        MySQLNullBitmapTest.class,
        AllMySQLBinaryProtocolTests.class, 
        MySQLFieldCountPacketTest.class,
        MySQLColumnDefinition41PacketTest.class,
        MySQLTextResultSetRowPacketTest.class,
        MySQLComFieldListPacketTest.class,
        MySQLComQueryPacketTest.class,
        MySQLComStmtPreparePacketTest.class,
        MySQLComStmtPrepareMySQLOKPacketTest.class,
        MySQLBinaryResultSetRowPacketTest.class,
        MySQLComStmtExecutePacketTest.class,
        MySQLComStmtClosePacketTest.class,
        MySQLComInitDbPacketTest.class,
        MySQLComPingPacketTest.class,
        MySQLComQuitPacketTest.class,
        MySQLUnsupportedCommandPacketTest.class
})
public final class AllMySQLMySQLCommandPacketTests {
}
