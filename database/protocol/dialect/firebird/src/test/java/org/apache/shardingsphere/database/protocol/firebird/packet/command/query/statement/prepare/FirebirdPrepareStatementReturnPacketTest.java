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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FirebirdPrepareStatementReturnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWrite() {
        FirebirdPrepareStatementReturnPacket packet = new FirebirdPrepareStatementReturnPacket();
        packet.setType(FirebirdSQLInfoReturnValue.SELECT);
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.INTEGER, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        FirebirdReturnColumnPacket columnPacket =
                new FirebirdReturnColumnPacket(Collections.singleton(FirebirdSQLInfoPacketType.DESCRIBE_END), 1, table, column, "", "", "", null);
        packet.getDescribeSelect().add(columnPacket);
        packet.getDescribeBind().add(columnPacket);
        packet.write(payload);
        verify(payload).writeInt1(FirebirdSQLInfoPacketType.STMT_TYPE.getCode());
        verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
}
