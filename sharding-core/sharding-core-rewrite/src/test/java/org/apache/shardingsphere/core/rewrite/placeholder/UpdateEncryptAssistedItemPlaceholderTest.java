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

package org.apache.shardingsphere.core.rewrite.placeholder;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UpdateEncryptAssistedItemPlaceholderTest {
    
    private UpdateEncryptAssistedItemPlaceholder assistedItemPlaceholder;
    
    @Test
    public void assertToStringWithPlaceholderWithAssistedColumn() {
        assistedItemPlaceholder = new UpdateEncryptAssistedItemPlaceholder("column_x", "column_assist");
        assertThat(assistedItemPlaceholder.toString(), is("column_x = ?, column_assist = ?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithAssistedColumn() {
        assistedItemPlaceholder = new UpdateEncryptAssistedItemPlaceholder("column_x", "a", "column_assist", 1);
        assertThat(assistedItemPlaceholder.toString(), is("column_x = 'a', column_assist = 1"));
        assertThat(assistedItemPlaceholder.getColumnName(), is("column_x"));
        assertThat(assistedItemPlaceholder.getAssistedColumnName(), is("column_assist"));
        assertThat(assistedItemPlaceholder.getColumnValue(), is((Comparable) "a"));
        assertThat(assistedItemPlaceholder.getAssistedColumnValue(), is((Comparable) 1));
        assertThat(assistedItemPlaceholder.getParameterMarkerIndex(), is(-1));
    }
}
