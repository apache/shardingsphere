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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import org.apache.shardingsphere.mcp.core.tool.handler.metadata.MetadataSearchResourceUriFactory.MetadataResourceUris;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MetadataSearchResourceUriFactoryTest {
    
    @Test
    void assertCreateDatabaseResourceKinds() {
        MetadataResourceUris actual = new MetadataSearchResourceUriFactory().create("logic_db", "", SupportedMCPMetadataObjectType.DATABASE, "", "", "");
        assertThat(actual.resource().get("resource_kind"), is("database"));
        assertThat(actual.parentResource().get("resource_kind"), is("database"));
        assertThat(actual.nextResources().stream().map(each -> each.get("resource_kind")).toList(), is(List.of("database", "schema")));
    }
    
    @Test
    void assertCreateTableResourceKinds() {
        MetadataResourceUris actual = new MetadataSearchResourceUriFactory().create("logic_db", "public", SupportedMCPMetadataObjectType.TABLE, "orders", "", "");
        assertThat(actual.resource().get("resource_kind"), is("table"));
        assertThat(actual.parentResource().get("resource_kind"), is("table"));
        assertThat(actual.nextResources().stream().map(each -> each.get("resource_kind")).toList(), is(List.of("column", "index")));
    }
    
    @Test
    void assertCreateStorageUnitResourceKinds() {
        MetadataResourceUris actual = new MetadataSearchResourceUriFactory().create("logic_db", "", SupportedMCPMetadataObjectType.STORAGE_UNIT, "", "", "write_ds");
        assertThat(actual.resource().get("resource_kind"), is("storage-unit"));
        assertThat(actual.parentResource().get("resource_kind"), is("storage-unit"));
        assertThat(actual.nextResources().stream().map(each -> each.get("resource_kind")).toList(), is(List.of("storage-unit")));
    }
}
