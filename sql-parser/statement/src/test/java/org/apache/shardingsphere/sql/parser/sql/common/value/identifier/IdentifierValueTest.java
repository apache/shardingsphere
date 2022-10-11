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

package org.apache.shardingsphere.sql.parser.sql.common.value.identifier;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class IdentifierValueTest {
    
    @Test
    public void assertGetIdentifierValueWithSingleQuote() {
        String text = "'ms_group_${0..1}'";
        assertThat(new IdentifierValue(text).getValue(), is("ms_group_${0..1}"));
    }
    
    @Test
    public void assertGetIdentifierValueWithQuote() {
        String text = "\"ms_group_${0..1}\"";
        assertThat(new IdentifierValue(text).getValue(), is("ms_group_${0..1}"));
    }
    
    @Test
    public void assertGetIdentifierValueWithBackQuote() {
        String text = "`t_order`";
        assertThat(new IdentifierValue(text).getValue(), is("t_order"));
    }
    
    @Test
    public void assertGetIdentifierValueWithBracket() {
        String text = "ds_${[1,2]}.t_order";
        assertThat(new IdentifierValue(text).getValue(), is("ds_${1,2}.t_order"));
    }
    
    @Test
    public void assertGetIdentifierValueWithReservedBracket() {
        String text = "ds_${[1,2]}.t_order";
        assertThat(new IdentifierValue(text, "[]").getValue(), is("ds_${[1,2]}.t_order"));
    }
}
