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

package org.apache.shardingsphere.traffic.algorithm.traffic.hint;

import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficValue;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SQLHintTrafficAlgorithmTest {
    
    private SQLHintTrafficAlgorithm sqlHintAlgorithm;
    
    @Before
    public void setUp() {
        sqlHintAlgorithm = new SQLHintTrafficAlgorithm();
        sqlHintAlgorithm.getProps().put("foo", "bar");
        sqlHintAlgorithm.getProps().put("test", "234");
        sqlHintAlgorithm.init();
    }
    
    @Test
    public void assertMatchWhenSQLHintAllMatch() {
        assertTrue(sqlHintAlgorithm.match(new HintTrafficValue<>("/* ShardingSphere hint: foo=bar , test=234 */")));
    }
    
    @Test
    public void assertMatchWhenSQLHintOneMatch() {
        assertFalse(sqlHintAlgorithm.match(new HintTrafficValue<>("/* ShardingSphere hint: foo=bar */")));
    }
    
    @Test
    public void assertGetType() {
        assertThat(sqlHintAlgorithm.getType(), is("SQL_HINT"));
    }
}
