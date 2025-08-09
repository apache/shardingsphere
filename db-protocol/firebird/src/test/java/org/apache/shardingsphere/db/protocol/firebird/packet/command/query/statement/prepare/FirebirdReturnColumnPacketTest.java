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
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdReturnColumnPacketTest {
    
    @Test
    void assertWrite() {
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.INTEGER, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        FirebirdReturnColumnPacket packet = new FirebirdReturnColumnPacket(Arrays.asList(
                FirebirdSQLInfoPacketType.SQLDA_SEQ,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.SUB_TYPE,
                FirebirdSQLInfoPacketType.SCALE,
                FirebirdSQLInfoPacketType.LENGTH,
                FirebirdSQLInfoPacketType.FIELD,
                FirebirdSQLInfoPacketType.ALIAS,
                FirebirdSQLInfoPacketType.RELATION,
                FirebirdSQLInfoPacketType.RELATION_ALIAS,
                FirebirdSQLInfoPacketType.OWNER,
                FirebirdSQLInfoPacketType.DESCRIBE_END), 1, table, column, "t", "c", "o");
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.SQLDA_SEQ.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(1));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.TYPE.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(FirebirdBinaryColumnType.valueOfJDBCType(Types.INTEGER).getValue() + 1));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.SUB_TYPE.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(FirebirdBinaryColumnType.valueOfJDBCType(Types.INTEGER).getSubtype()));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.SCALE.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(0));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.LENGTH.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.getByteBuf().readIntLE(), is(FirebirdBinaryColumnType.valueOfJDBCType(Types.INTEGER).getLength()));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.FIELD.getCode()));
        int len = result.readInt2LE();
        assertThat(len, is("col".getBytes(StandardCharsets.UTF_8).length));
        assertThat(result.readBytes(len).toString(StandardCharsets.UTF_8), is("col"));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.ALIAS.getCode()));
        len = result.readInt2LE();
        assertThat(len, is("c".getBytes(StandardCharsets.UTF_8).length));
        assertThat(result.readBytes(len).toString(StandardCharsets.UTF_8), is("c"));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.RELATION.getCode()));
        len = result.readInt2LE();
        assertThat(len, is("tbl".getBytes(StandardCharsets.UTF_8).length));
        assertThat(result.readBytes(len).toString(StandardCharsets.UTF_8), is("tbl"));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.RELATION_ALIAS.getCode()));
        len = result.readInt2LE();
        assertThat(len, is("t".getBytes(StandardCharsets.UTF_8).length));
        assertThat(result.readBytes(len).toString(StandardCharsets.UTF_8), is("t"));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.OWNER.getCode()));
        len = result.readInt2LE();
        assertThat(len, is("o".getBytes(StandardCharsets.UTF_8).length));
        assertThat(result.readBytes(len).toString(StandardCharsets.UTF_8), is("o"));
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.DESCRIBE_END.getCode()));
    }
}
