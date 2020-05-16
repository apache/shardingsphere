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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLStatusFlagTest {
    
    @Test
    public void assertGetValue() {
        assertThat(MySQLStatusFlag.SERVER_STATUS_IN_TRANS.getValue(), is(0x0001));
    }
    
    @Test
    public void assertValueOfByInteger() {
        assertThat(MySQLStatusFlag.valueOf(0x0001), is(MySQLStatusFlag.SERVER_STATUS_IN_TRANS));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfByIntegerFailure() {
        MySQLStatusFlag.valueOf(0x0011);
    }
}
