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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl.MySQLUnsignedSmallintHandler;
import org.junit.Test;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MySQLUnsignedSmallintHandlerTest {
    
    private final MySQLUnsignedSmallintHandler handler = new MySQLUnsignedSmallintHandler();
    
    @Test
    public void assertHandle() {
        Serializable actual = handler.handle((short) 1);
        assertThat(actual, is(1));
        actual = handler.handle((short) -1);
        assertThat(actual, is(65535));
    }
}
