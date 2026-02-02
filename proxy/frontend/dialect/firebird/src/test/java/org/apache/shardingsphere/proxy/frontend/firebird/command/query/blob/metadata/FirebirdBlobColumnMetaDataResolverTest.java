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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.metadata;

import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdBlobInfoRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdBlobColumnMetaDataResolverTest {
    
    private FirebirdBlobColumnMetaDataResolver resolver;
    
    @BeforeEach
    void setUp() {
        resolver = new FirebirdBlobColumnMetaDataResolver("foo_db");
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl", Collections.emptyMap());
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "bar_tbl", Collections.emptyMap());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createArguments")
    void assertResolve(final String name, final String tableName, final ShardingSphereColumn column, final Map<String, Integer> registry, final boolean expectedBlob, final Integer expectedSubtype) {
        FirebirdBlobInfoRegistry.refreshTable("foo_db", tableName, registry);
        Collection<ShardingSphereColumn> columns = null == column ? Collections.emptyList() : Collections.singleton(column);
        ShardingSphereTable table = null == tableName ? null : new ShardingSphereTable(tableName, columns, Collections.emptyList(), Collections.emptyList());
        FirebirdBlobColumnMetaData actual = resolver.resolve(table, column);
        assertThat(actual.isBlobColumn(), is(expectedBlob));
        assertThat(actual.getBlobSubtype(), is(expectedSubtype));
    }
    
    private static Stream<Arguments> createArguments() {
        return Stream.of(
                Arguments.of("blob-registered-with-subtype", "foo_tbl",
                        new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true), Collections.singletonMap("id", 7), true, 7),
                Arguments.of("blob-type-without-registry", "foo_tbl",
                        new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true), Collections.emptyMap(), true, null),
                Arguments.of("non-blob", "bar_tbl",
                        new ShardingSphereColumn("name", Types.VARCHAR, false, false, true, true, false, true), Collections.emptyMap(), false, null),
                Arguments.of("null-table", null,
                        new ShardingSphereColumn("name", Types.VARCHAR, false, false, true, true, false, true), Collections.emptyMap(), false, null),
                Arguments.of("null-column", "foo_tbl", null, Collections.emptyMap(), false, null));
    }
}
