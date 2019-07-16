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

package org.apache.shardingsphere.core.rewrite.token;

import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptAssistedItemToken;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UpdateEncryptAssistedItemTokenTest {
    
    private UpdateEncryptAssistedItemToken assistedItemToken;
    
    @Test
    public void assertToStringWithTokenWithAssistedColumn() {
        assistedItemToken = new UpdateEncryptAssistedItemToken(0, 0, "column_x", "column_assist");
        assertThat(assistedItemToken.toString(), is("column_x = ?, column_assist = ?"));
    }
    
    @Test
    public void assertToStringWithoutTokenWithAssistedColumn() {
        assistedItemToken = new UpdateEncryptAssistedItemToken(0, 0, "column_x", "a", "column_assist", 1);
        assertThat(assistedItemToken.toString(), is("column_x = 'a', column_assist = 1"));
        assertThat(assistedItemToken.getColumnName(), is("column_x"));
        assertThat(assistedItemToken.getAssistedColumnName(), is("column_assist"));
        assertThat(assistedItemToken.getColumnValue(), is((Object) "a"));
        assertThat(assistedItemToken.getAssistedColumnValue(), is((Object) 1));
        assertThat(assistedItemToken.getParameterMarkerIndex(), is(-1));
    }
}
