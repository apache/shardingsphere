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

package org.apache.shardingsphere.infra.metadata.caseinsensitive;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CaseInsensitiveIdentifierTest {
    
    @Test
    void assertEquals() {
        assertThat(new CaseInsensitiveIdentifier("t_order"), is(new CaseInsensitiveIdentifier("T_ORDER")));
    }
    
    @Test
    void assertToString() {
        CaseInsensitiveIdentifier actual = new CaseInsensitiveIdentifier("T_ORDER");
        assertThat(actual.toString(), is("T_ORDER"));
    }
}
