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

package org.apache.shardingsphere.sql.parser.sql.segment.generic.table;

import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SimpleTableSegmentTest {
    
    @Test
    public void getStartIndexWithoutOwner() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(10, 13, new IdentifierValue("tbl"));
        assertThat(tableSegment.getStartIndex(), is(10));
    }
    
    @Test
    public void getStartIndexWithOwner() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(10, 13, new IdentifierValue("tbl"));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("o")));
        assertThat(tableSegment.getStartIndex(), is(7));
    }
}
