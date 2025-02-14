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

package org.apache.shardingsphere.mode.node.path.metadata;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DatabaseMetaDataNodePathGeneratorTest {
    
    @Test
    void assertGetRootPath() {
        MatcherAssert.assertThat(DatabaseMetaDataNodePathGenerator.getRootPath(), is("/metadata"));
    }
    
    @Test
    void assertGetDatabasePath() {
        assertThat(DatabaseMetaDataNodePathGenerator.getDatabasePath("foo_db"), is("/metadata/foo_db"));
    }
    
    @Test
    void assertGetSchemaRootPath() {
        assertThat(DatabaseMetaDataNodePathGenerator.getSchemaRootPath("foo_db"), is("/metadata/foo_db/schemas"));
    }
    
    @Test
    void assertGetSchemaPath() {
        assertThat(DatabaseMetaDataNodePathGenerator.getSchemaPath("foo_db", "foo_schema"), is("/metadata/foo_db/schemas/foo_schema"));
    }
}
