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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.unsigned.impl;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLBinlogUnsignedBigintHandlerTest {
    
    private final MySQLBinlogUnsignedBigintHandler handler = new MySQLBinlogUnsignedBigintHandler();
    
    @Test
    void assertHandle() {
        Serializable actual = handler.handle(1L);
        assertThat(actual, instanceOf(BigInteger.class));
        assertThat(actual, is(new BigInteger("1")));
        actual = handler.handle(-1L);
        assertThat(actual, instanceOf(BigInteger.class));
        assertThat(actual, is(new BigInteger("18446744073709551615")));
    }
}
