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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class PostgreSQLBinaryProtocolValueFactoryTest {
    
    @Test
    public void assertGetBinaryProtocolValue() {
        Map<PostgreSQLColumnType, Class<? extends PostgreSQLBinaryProtocolValue>> protocolValueMap = new LinkedHashMap<>();
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED, PostgreSQLUnspecifiedBinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_VARCHAR, PostgreSQLStringBinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_INT8, PostgreSQLInt8BinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, PostgreSQLInt4BinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_INT2, PostgreSQLInt2BinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_FLOAT8, PostgreSQLDoubleBinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_FLOAT4, PostgreSQLFloatBinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_DATE, PostgreSQLDateBinaryProtocolValue.class);
        protocolValueMap.put(PostgreSQLColumnType.POSTGRESQL_TYPE_TIMESTAMP, PostgreSQLTimeBinaryProtocolValue.class);
        for (Map.Entry<PostgreSQLColumnType, Class<? extends PostgreSQLBinaryProtocolValue>> each : protocolValueMap.entrySet()) {
            PostgreSQLBinaryProtocolValue protocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(each.getKey());
            assertNotNull(protocolValue);
            assertEquals(each.getValue(), protocolValue.getClass());
        }
    }
    
}
