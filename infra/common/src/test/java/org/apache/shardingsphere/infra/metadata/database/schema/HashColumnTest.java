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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class HashColumnTest {
    
    @Test
    void assertEquals() {
        assertThat(new HashColumn("col", "tbl"), not(new Object()));
        assertThat(new HashColumn("col", "tbl"), is(new HashColumn("COL", "TBL")));
        assertThat(new HashColumn("col", "tbl"), not(new HashColumn("col1", "tbl")));
        assertThat(new HashColumn("col", "tbl"), not(new HashColumn("col", "tbl1")));
        HashColumn column1 = new HashColumn("col", "tbl");
        HashColumn column2 = new HashColumn("COL", "TBL");
        assertThat(column1.equals(column2), is(true));
        assertThat(column2.equals(column1), is(true));
    }
    
    @Test
    void assertHashCode() {
        assertThat(new HashColumn("col", "tbl").hashCode(), is(new HashColumn("COL", "TBL").hashCode()));
        assertThat(new HashColumn("col", "tbl").hashCode(), not(new HashColumn("different_col", "tbl").hashCode()));
    }
}
