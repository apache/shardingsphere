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

package org.apache.shardingsphere.mode.node.path.metadata.database;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TableNodePathGeneratorTest {
    
    @Test
    void assertGetRootPath() {
        assertThat(new TableNodePathGenerator("foo_db", "foo_schema").getRootPath(), is("/metadata/foo_db/schemas/foo_schema/tables"));
    }
    
    @Test
    void assertGetPath() {
        assertThat(new TableNodePathGenerator("foo_db", "foo_schema").getPath("foo_tbl"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl"));
    }
    
    @Test
    void assertGetVersion() {
        assertThat(new TableNodePathGenerator("foo_db", "foo_schema").getVersion("foo_tbl").getActiveVersionPath(), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
        assertThat(new TableNodePathGenerator("foo_db", "foo_schema").getVersion("foo_tbl").getVersionsPath(), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
        assertThat(new TableNodePathGenerator("foo_db", "foo_schema").getVersion("foo_tbl").getVersionPath(0), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0"));
    }
}
