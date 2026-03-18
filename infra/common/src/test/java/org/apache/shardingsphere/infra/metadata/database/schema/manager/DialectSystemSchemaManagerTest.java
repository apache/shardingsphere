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

package org.apache.shardingsphere.infra.metadata.database.schema.manager;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialectSystemSchemaManagerTest {
    
    @Test
    void assertSystemTableFoundWithoutSchema() {
        DialectSystemSchemaManager manager = new DialectSystemSchemaManager();
        manager.putTable("information_schema", "tbl");
        assertTrue(manager.isSystemTable(null, "tbl"));
    }
    
    @Test
    void assertSystemTableMissingWithoutSchema() {
        assertFalse(new DialectSystemSchemaManager().isSystemTable(null, "absent"));
    }
    
    @Test
    void assertSystemTableFoundWithSchema() {
        DialectSystemSchemaManager manager = new DialectSystemSchemaManager();
        manager.putTable("public", "t_order");
        assertTrue(manager.isSystemTable("public", "t_order"));
    }
    
    @Test
    void assertSystemTableMissingWithSchema() {
        DialectSystemSchemaManager manager = new DialectSystemSchemaManager();
        manager.putTable("public", "t_order");
        assertFalse(manager.isSystemTable("public", "t_user"));
    }
    
    @Test
    void assertAllTablesExistInCollection() {
        DialectSystemSchemaManager manager = new DialectSystemSchemaManager();
        manager.putTable("public", "t_order");
        manager.putTable("public", "t_user");
        assertTrue(manager.isSystemTable("public", Arrays.asList("t_order", "t_user")));
    }
    
    @Test
    void assertMissingTableDetectedInCollection() {
        DialectSystemSchemaManager manager = new DialectSystemSchemaManager();
        manager.putTable("public", "t_order");
        assertFalse(manager.isSystemTable("public", Arrays.asList("t_order", "t_user")));
    }
    
    @Test
    void assertEmptyTablesForAbsentSchema() {
        assertTrue(new DialectSystemSchemaManager().getTables("absent").isEmpty());
    }
    
    @Test
    void assertLoadInputStreamsFromResources() {
        DialectSystemSchemaManager manager = new DialectSystemSchemaManager();
        manager.putResource("public", "dialect-system-resource.txt");
        List<String> actual = manager.getAllInputStreams("public").stream().map(this::readString).collect(Collectors.toList());
        assertThat(actual.get(0).trim(), is("resource-content"));
    }
    
    @Test
    void assertEmptyInputStreamListForAbsentResource() {
        assertThat(new DialectSystemSchemaManager().getAllInputStreams("absent"), is(Collections.emptyList()));
    }
    
    @SneakyThrows(IOException.class)
    private String readString(final InputStream inputStream) {
        try (
                InputStream in = inputStream;
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[128];
            int length;
            while (-1 != (length = in.read(buffer))) {
                out.write(buffer, 0, length);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
