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

package org.apache.shardingsphere.infra.instance.definition;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

public class InstanceDefinitionTest {

    private static final String INSTANCE = "instance1";

    private static final String PORT = "8080";

    private static final String IP = "192.168.1.88";

    private static final String ATTRIBUTES = "192.168.1.88@8080";

    private InstanceDefinition instanceDefinition;

    @Test
    public void testWithId() {
        try (MockedStatic<IpUtils> mocked = mockStatic(IpUtils.class)) {
            mocked.when(IpUtils::getIp).thenReturn(IP);
            instanceDefinition = new InstanceDefinition(INSTANCE);
            assertEquals(INSTANCE, instanceDefinition.getInstanceId());
            assertEquals(InstanceType.JDBC, instanceDefinition.getInstanceType());
            assertEquals(IP, instanceDefinition.getIp());
            assertNotNull(instanceDefinition.getUniqueSign());
            assertTrue(instanceDefinition.getAttributes().startsWith(IP + "@"));
            mocked.verify(IpUtils::getIp);
        }
    }

    @Test
    public void testWithPort() {
        try (MockedStatic<IpUtils> mocked = mockStatic(IpUtils.class)) {
            mocked.when(IpUtils::getIp).thenReturn(IP);
            instanceDefinition = new InstanceDefinition(Integer.parseInt(PORT), INSTANCE);
            assertEquals(INSTANCE, instanceDefinition.getInstanceId());
            assertEquals(InstanceType.PROXY, instanceDefinition.getInstanceType());
            assertEquals(IP, instanceDefinition.getIp());
            assertEquals(PORT, instanceDefinition.getUniqueSign());
            assertEquals(ATTRIBUTES, instanceDefinition.getAttributes());
            mocked.verify(IpUtils::getIp);
        }
    }

    @Test
    public void testWithAttributes() {
        instanceDefinition = new InstanceDefinition(InstanceType.PROXY, INSTANCE, ATTRIBUTES);
        assertEquals(INSTANCE, instanceDefinition.getInstanceId());
        assertEquals(InstanceType.PROXY, instanceDefinition.getInstanceType());
        assertEquals(IP, instanceDefinition.getIp());
        assertEquals(PORT, instanceDefinition.getUniqueSign());
        assertEquals(ATTRIBUTES, instanceDefinition.getAttributes());
    }
}
