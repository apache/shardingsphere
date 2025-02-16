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

class ViewNodePathGeneratorTest {
    
    @Test
    void assertGetRootPath() {
        assertThat(new ViewNodePathGenerator("foo_db", "foo_schema").getRootPath(), is("/metadata/foo_db/schemas/foo_schema/views"));
    }
    
    @Test
    void assertGetPath() {
        assertThat(new ViewNodePathGenerator("foo_db", "foo_schema").getPath("foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view"));
    }
    
    @Test
    void assertGetVersion() {
        assertThat(new ViewNodePathGenerator("foo_db", "foo_schema").getVersion("foo_view").getActiveVersionPath(), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version"));
        assertThat(new ViewNodePathGenerator("foo_db", "foo_schema").getVersion("foo_view").getVersionsPath(), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions"));
        assertThat(new ViewNodePathGenerator("foo_db", "foo_schema").getVersion("foo_view").getVersionPath(0), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0"));
    }
}
