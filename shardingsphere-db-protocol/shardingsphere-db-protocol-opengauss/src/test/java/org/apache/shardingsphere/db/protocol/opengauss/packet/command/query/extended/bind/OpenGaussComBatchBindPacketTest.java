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

package org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OpenGaussComBatchBindPacketTest {
    
    private static final byte[] BATCH_BIND_MESSAGE_BYTES = {
        'U', 0x00, 0x00, 0x00, 0x55, 0x00, 0x00, 0x00,
        0x03, 0x00, 'S', '_', '1', 0x00, 0x00, 0x03,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x31, 0x00,
        0x00, 0x00, 0x03, 0x46, 0x6f, 0x6f, 0x00, 0x00,
        0x00, 0x02, 0x31, 0x38, 0x00, 0x00, 0x00, 0x01,
        0x32, 0x00, 0x00, 0x00, 0x03, 0x42, 0x61, 0x72,
        0x00, 0x00, 0x00, 0x02, 0x33, 0x36, 0x00, 0x00,
        0x00, 0x01, 0x33, 0x00, 0x00, 0x00, 0x03, 0x54,
        0x6f, 0x6d, 0x00, 0x00, 0x00, 0x02, 0x35, 0x34,
        0x45, 0x00, 0x00, 0x00, 0x00, 0x00,
    };
    
    private static final int CONNECTION_ID = 1;
    
    @Test
    public void assertConstructOpenGaussComBatchBindPacket() {
        PostgreSQLPreparedStatementRegistry.getInstance().register(CONNECTION_ID);
        SQLStatement expectedSQLStatement = Mockito.mock(SQLStatement.class);
        List<PostgreSQLColumnType> columnTypes = Arrays.asList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, PostgreSQLColumnType.POSTGRESQL_TYPE_VARCHAR, PostgreSQLColumnType.POSTGRESQL_TYPE_INT4);
        PostgreSQLPreparedStatementRegistry.getInstance().register(CONNECTION_ID, "S_1", "", expectedSQLStatement, columnTypes);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(BATCH_BIND_MESSAGE_BYTES), StandardCharsets.UTF_8);
        assertThat(payload.readInt1(), is((int) 'U'));
        OpenGaussComBatchBindPacket actual = new OpenGaussComBatchBindPacket(payload);
        assertThat(actual.getStatementId(), is("S_1"));
        assertThat(actual.getEachGroupParametersCount(), is(3));
        assertThat(actual.getParameterFormats(), is(Arrays.asList(0, 0, 0)));
        assertTrue(actual.getResultFormats().isEmpty());
        List<List<Object>> actualParameterSets = actual.readParameterSets(
                Arrays.asList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, PostgreSQLColumnType.POSTGRESQL_TYPE_VARCHAR, PostgreSQLColumnType.POSTGRESQL_TYPE_INT4));
        assertThat(actualParameterSets.size(), is(3));
        List<List<Object>> expectedParameterSets = Arrays.asList(Arrays.asList(1, "Foo", 18), Arrays.asList(2, "Bar", 36), Arrays.asList(3, "Tom", 54));
        assertThat(actualParameterSets, is(expectedParameterSets));
    }
}
