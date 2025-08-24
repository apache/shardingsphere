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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command.packet.command;

import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class OpenGaussCommandPacketTypeTest {
    
    @Test
    void assertValueOfBatchBindCommand() {
        CommandPacketType actual = OpenGaussCommandPacketType.valueOf('U');
        assertThat(actual, is(OpenGaussCommandPacketType.BATCH_BIND_COMMAND));
    }
    
    @Test
    void assertValueOfPostgreSQLCommand() {
        CommandPacketType actual = OpenGaussCommandPacketType.valueOf('Q');
        assertThat(actual, isA(PostgreSQLCommandPacketType.class));
    }
}
