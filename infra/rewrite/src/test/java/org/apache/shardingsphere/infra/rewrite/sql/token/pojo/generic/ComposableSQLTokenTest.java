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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ComposableSQLTokenTest {
    
    @Test
    public void assertComposableSQLToken() {
        ComposableSQLToken composableSQLToken = new ComposableSQLToken(0, 1);
        SQLToken sqlToken = mock(SQLToken.class);
        when(sqlToken.getStartIndex()).thenReturn(2);
        when(sqlToken.compareTo(any(SQLToken.class))).thenReturn(0);
        composableSQLToken.addSQLToken(sqlToken);
        assertThat(composableSQLToken.getStartIndex(), is(0));
        assertThat(composableSQLToken.getStopIndex(), is(1));
        assertThat(composableSQLToken.getSqlTokens().size(), is(1));
        assertThat(composableSQLToken.getSqlTokens().iterator().next().compareTo(sqlToken), is(0));
    }
}
