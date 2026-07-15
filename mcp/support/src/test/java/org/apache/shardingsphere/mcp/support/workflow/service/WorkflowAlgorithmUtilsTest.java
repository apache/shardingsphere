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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class WorkflowAlgorithmUtilsTest {
    
    @Test
    void assertNormalizeAlgorithmType() {
        assertThat(WorkflowAlgorithmUtils.normalizeAlgorithmType(" fixture "), is("FIXTURE"));
    }
    
    @Test
    void assertGetAlgorithmType() {
        assertThat(WorkflowAlgorithmUtils.getAlgorithmType(Map.of("type", " fixture ")), is("FIXTURE"));
    }
    
    @Test
    void assertGetAlgorithmTypeWithFallbackKey() {
        assertThat(WorkflowAlgorithmUtils.getAlgorithmType(Map.of("name", " fixture "), "type", "name"), is("FIXTURE"));
    }
    
    @Test
    void assertGetAlgorithmTypeKeepsPresentPrimaryKey() {
        assertThat(WorkflowAlgorithmUtils.getAlgorithmType(Map.of("type", "", "name", "fixture"), "type", "name"), is(""));
    }
    
    @Test
    void assertContainsAlgorithm() {
        assertTrue(WorkflowAlgorithmUtils.containsAlgorithm(List.of(Map.of("name", " fixture ")), "fixture", "type", "name"));
    }
    
    @Test
    void assertCreatePropertiesTrimsValues() {
        Properties actualProperties = WorkflowAlgorithmUtils.createProperties(Map.of("aes-key-value", " 123456 "));
        assertThat(actualProperties.getProperty("aes-key-value"), is("123456"));
    }
    
    @Test
    void assertCreatePropertyMapReturnsEmptyForNull() {
        assertThat(WorkflowAlgorithmUtils.createPropertyMap(null), is(Map.of()));
    }
    
    @Test
    void assertCreatePropertyMapHandlesProperties() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", " 123456 ");
        assertThat(WorkflowAlgorithmUtils.createPropertyMap(props), is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesMap() {
        assertThat(WorkflowAlgorithmUtils.createPropertyMap(Map.of("aes-key-value", " 123456 ")), is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesString() {
        assertThat(WorkflowAlgorithmUtils.createPropertyMap("{'aes-key-value':'123456','iv':'abc'}"), is(Map.of("aes-key-value", "123456", "iv", "abc")));
    }
    
    @Test
    void assertAlgorithmServiceAvailable() {
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class)) {
            assertTrue(WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(TypedSPIFixture.class, "FIXTURE", Map.of("foo", "bar")));
            ignored.verify(() -> TypedSPILoader.checkService(TypedSPIFixture.class, "FIXTURE", WorkflowAlgorithmUtils.createProperties(Map.of("foo", "bar"))));
        }
    }
    
    @Test
    void assertAlgorithmServiceAvailableWithTrimmedType() {
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class)) {
            assertTrue(WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(TypedSPIFixture.class, " FIXTURE ", Map.of()));
            ignored.verify(() -> TypedSPILoader.checkService(TypedSPIFixture.class, "FIXTURE", new Properties()));
        }
    }
    
    @Test
    void assertAlgorithmServiceUnavailable() {
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(TypedSPIFixture.class, "MISSING", new Properties()))
                    .thenThrow(new ServiceProviderNotFoundException(TypedSPIFixture.class, "MISSING"));
            assertFalse(WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(TypedSPIFixture.class, "MISSING", Map.of()));
        }
    }
    
    @Test
    void assertSecretReferenceOnlyRequiresTypeAvailability() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(TypedSPIFixture.class)).thenReturn(List.of(new TypedSPIFixture()));
            assertTrue(WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(TypedSPIFixture.class, "fixture", Map.of("secret-key", "secret_reference:primary.secret-key")));
            typedSPILoader.verifyNoInteractions();
        }
    }
    
    @Test
    void assertSecretReferenceRejectsMissingType() {
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(TypedSPIFixture.class)).thenReturn(List.of(new TypedSPIFixture()));
            assertFalse(WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(TypedSPIFixture.class, "MISSING", Map.of("secret-key", "secret_reference:primary.secret-key")));
        }
    }
    
    private static final class TypedSPIFixture implements TypedSPI {
        
        @Override
        public Object getType() {
            return "FIXTURE";
        }
        
        @Override
        public Collection<Object> getTypeAliases() {
            return List.of("FIXTURE_ALIAS");
        }
    }
}
