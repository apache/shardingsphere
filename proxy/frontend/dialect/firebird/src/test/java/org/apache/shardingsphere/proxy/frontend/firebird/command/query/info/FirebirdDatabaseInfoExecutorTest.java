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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset.FirebirdCharacterSets;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(FirebirdCharacterSets.class)
class FirebirdDatabaseInfoExecutorTest {
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecute() {
        FirebirdDatabaseInfoExecutor executor = new FirebirdDatabaseInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(responsePacket.getData(), isA(FirebirdDatabaseInfoReturnPacket.class));
    }
    
    @ParameterizedTest(name = "attachment charset {0}")
    @CsvSource({"NONE,0", "UNICODE_FSS,3", "UTF8,4", "WIN1251,52"})
    void assertExecuteWithAttachmentCharset(final String charsetName, final int expected) {
        when(packet.getInfoItems()).thenReturn(Collections.singletonList(FirebirdDatabaseInfoPacketType.ATT_CHARSET));
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.ATTACHMENT_CHARSET).get()).thenReturn(charsetName);
        when(FirebirdCharacterSets.findCharacterSetId(charsetName)).thenReturn(expected);
        Collection<DatabasePacket> actual = new FirebirdDatabaseInfoExecutor(packet, connectionSession).execute();
        FirebirdDatabaseInfoReturnPacket actualData = (FirebirdDatabaseInfoReturnPacket) ((FirebirdGenericResponsePacket) actual.iterator().next()).getData();
        assertThat(actualData.getAttachmentCharsetId(), is(expected));
    }
}
