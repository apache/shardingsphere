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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.prepare;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdPrepareStatementReturnPacketTest {
    
    @Test
    void assertWrite() {
        FirebirdPrepareStatementReturnPacket packet = new FirebirdPrepareStatementReturnPacket();
        packet.setType(FirebirdSQLInfoReturnValue.SELECT);
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.INTEGER, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        FirebirdReturnColumnPacket columnPacket = new FirebirdReturnColumnPacket(Collections.singleton(FirebirdSQLInfoPacketType.DESCRIBE_END), 1, table, column, "", "", "");
        packet.getDescribeSelect().add(columnPacket);
        packet.getDescribeBind().add(columnPacket);
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.STMT_TYPE.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(FirebirdSQLInfoReturnValue.SELECT.getCode()));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.SELECT.getCode()));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.DESCRIBE_VARS.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(1));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.DESCRIBE_END.getCode()));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.BIND.getCode()));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.DESCRIBE_VARS.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(1));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.DESCRIBE_END.getCode()));
        assertThat(result.readInt1(), is(FirebirdCommonInfoPacketType.END.getCode()));
    }
}
