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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic;

import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SubstituteColumnDefinitionTokenTest {
    
    @Test
    void assertToStringForRemoveTrailingDelimiterForLastColumn() {
        Collection<SQLToken> tokens = Arrays.asList(new ColumnDefinitionToken("id", "INT", 0), new ColumnDefinitionToken("name", "VARCHAR", 4));
        assertThat(new SubstituteColumnDefinitionToken(0, 1, true, tokens).toString(), is("id INT, name VARCHAR"));
    }
    
    @Test
    void assertToStringWhenNotLastColumn() {
        Collection<SQLToken> tokens = Collections.singleton(new ColumnDefinitionToken("id", "INT", 0));
        assertThat(new SubstituteColumnDefinitionToken(0, 0, false, tokens).toString(), is("id INT, "));
    }
}
