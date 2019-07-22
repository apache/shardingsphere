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

package org.apache.shardingsphere.core.rewrite.token.pojo;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class UpdateEncryptParameterColumnTokenTest {
    
    private UpdateEncryptParameterColumnToken updateEncryptParameterColumnToken;
    
    @Before
    public void setUp() {
        updateEncryptParameterColumnToken = new UpdateEncryptParameterColumnToken(0, 1);
        updateEncryptParameterColumnToken.addUpdateColumn("c1");
        updateEncryptParameterColumnToken.addUpdateColumn("c2");
    }
    
    @Test
    public void assertToString() {
        assertThat(updateEncryptParameterColumnToken.toString(), is("c1 = ?, c2 = ?"));
    }
    
    @Test
    public void assertGetColumnNames() {
        assertThat(updateEncryptParameterColumnToken.getColumnNames().size(), is(2));
    }
}
