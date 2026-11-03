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

package org.apache.shardingsphere.mcp.core.metadata;

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GovernanceMetadataQueryServiceTest {
    
    private final GovernanceMetadataQueryService service = new GovernanceMetadataQueryService();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("storageUnitAttributeCases")
    void assertQueryStorageUnitsWithOtherAttributes(final String name, final Object attributes, final Object expected) {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("foo_db", "SHOW STORAGE UNITS FROM foo_db")).thenReturn(List.of(Map.of("name", "foo_ds", "other_attributes", attributes)));
        List<Map<String, Object>> actual = service.queryStorageUnits(queryFacade, "foo_db");
        assertThat(actual, is(List.of(Map.of("name", "foo_ds", "other_attributes", expected))));
    }
    
    private static Stream<Arguments> storageUnitAttributeCases() {
        return Stream.of(
                Arguments.of("nested collections", List.of(Map.of("PASSWORD", "foo_password"), List.of(Map.of("pool", 8))), List.of(Map.of("PASSWORD", "******"), List.of(Map.of("pool", 8)))),
                Arguments.of("JSON array", "[{\"secret\":\"foo_secret\"},7]", List.of(Map.of("secret", "******"), 7)),
                Arguments.of("ordinary text", "foo_pool=8", "foo_pool=8"),
                Arguments.of("sensitive text", "PASSWORD=foo_password", "******"),
                Arguments.of("malformed sensitive JSON", "{\"token\":", "******"),
                Arguments.of("malformed ordinary JSON", "{\"pool\":", "{\"pool\":"),
                Arguments.of("ordinary scalar", 8, 8),
                Arguments.of("ordinary map", Map.of("pool", 8), Map.of("pool", 8)));
    }
    
    @Test
    void assertQueryStorageUnits() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW STORAGE UNITS FROM logic_db")).thenReturn(List.of(Map.of(
                "name", "write_ds", "password", "root", "key-value", "plain",
                "other_attributes", "{\"credential\":\"secret\",\"healthCheckProperties\":{\"token\":\"abc\"},\"key\":\"visible\"}")));
        List<Map<String, Object>> actual = service.queryStorageUnits(queryFacade, "logic_db");
        assertThat(actual.getFirst().get("password"), is("******"));
        assertThat(actual.getFirst().get("key-value"), is("plain"));
        Map<?, ?> actualOtherAttributes = (Map<?, ?>) actual.getFirst().get("other_attributes");
        assertThat(actualOtherAttributes.get("credential"), is("******"));
        assertThat(((Map<?, ?>) actualOtherAttributes.get("healthCheckProperties")).get("token"), is("******"));
        assertThat(actualOtherAttributes.get("key"), is("visible"));
    }
    
    @Test
    void assertQueryStorageUnit() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW STORAGE UNITS FROM logic_db")).thenReturn(List.of(Map.of("name", "write_ds"), Map.of("name", "read_ds")));
        List<Map<String, Object>> actual = service.queryStorageUnit(queryFacade, "logic_db", "write_ds");
        assertThat(actual, is(List.of(Map.of("name", "write_ds"))));
    }
    
    @Test
    void assertQueryRulesUsedStorageUnit() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW RULES USED STORAGE UNIT write_ds FROM logic_db"))
                .thenReturn(List.of(Map.of("type", "readwrite_splitting", "name", "ms_group_0")));
        List<Map<String, Object>> actual = service.queryRulesUsedStorageUnit(queryFacade, "logic_db", "write_ds");
        assertThat(actual, is(List.of(Map.of("type", "readwrite_splitting", "name", "ms_group_0"))));
    }
    
    @Test
    void assertQuerySingleTables() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW SINGLE TABLES FROM logic_db")).thenReturn(List.of(Map.of("table_name", "t_user", "storage_unit_name", "ds_0")));
        List<Map<String, Object>> actual = service.querySingleTables(queryFacade, "logic_db");
        assertThat(actual, is(List.of(Map.of("table_name", "t_user", "storage_unit_name", "ds_0"))));
    }
    
    @Test
    void assertQuerySingleTable() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW SINGLE TABLE t_user FROM logic_db")).thenReturn(List.of(Map.of("table_name", "t_user", "storage_unit_name", "ds_0")));
        List<Map<String, Object>> actual = service.querySingleTable(queryFacade, "logic_db", "t_user");
        assertThat(actual, is(List.of(Map.of("table_name", "t_user", "storage_unit_name", "ds_0"))));
    }
    
    @Test
    void assertQueryDefaultSingleTableStorageUnit() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM logic_db")).thenReturn(List.of(Map.of("storage_unit_name", "ds_0")));
        List<Map<String, Object>> actual = service.queryDefaultSingleTableStorageUnit(queryFacade, "logic_db");
        assertThat(actual, is(List.of(Map.of("storage_unit_name", "ds_0"))));
    }
}
