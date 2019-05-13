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
    
    private UpdateEncryptItemPlaceholder updateEncryptItemPlaceholder;
    
    @Test
    public void assertToStringWithPlaceholderWithoutAssistedColumn() {
        updateEncryptItemPlaceholder = new UpdateEncryptItemPlaceholder("column_x");
        assertThat(updateEncryptItemPlaceholder.toString(), is("column_x = ?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithoutAssistedColumn() {
        updateEncryptItemPlaceholder = new UpdateEncryptItemPlaceholder("column_x", 1);
        assertThat(updateEncryptItemPlaceholder.toString(), is("column_x = 1"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithAssistedColumn() {
        updateEncryptItemPlaceholder = new UpdateEncryptItemPlaceholder("column_x", "column_assist");
        assertThat(updateEncryptItemPlaceholder.toString(), is("column_x = ?, column_assist = ?"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithAssistedColumn() {
        updateEncryptItemPlaceholder = new UpdateEncryptItemPlaceholder("column_x", "a", "column_assist", 1);
        assertThat(updateEncryptItemPlaceholder.toString(), is("column_x = 'a', column_assist = 1"));
        assertThat(updateEncryptItemPlaceholder.getColumnName(), is("column_x"));
        assertThat(updateEncryptItemPlaceholder.getAssistedColumnName(), is("column_assist"));
        assertThat(updateEncryptItemPlaceholder.getColumnValue(), is((Comparable) "a"));
        assertThat(updateEncryptItemPlaceholder.getAssistedColumnValue(), is((Comparable) 1));
        assertThat(updateEncryptItemPlaceholder.getParameterMarkerIndex(), is(-1));
    }
}
