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

package org.apache.shardingsphere.scaling.mysql.binlog;

import com.google.gson.Gson;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BinlogPositionTest {
    
    private static final Gson GSON = new Gson();
    
    @Test
    public void assertCompareTo() {
        int position = 10;
        BinlogPosition binlogPosition = new BinlogPosition("mysql-bin.000002", position);
        assertThat(binlogPosition.compareTo(new BinlogPosition("mysql-bin.000002", position)), is(0));
        assertThat(binlogPosition.compareTo(new BinlogPosition("mysql-bin.000001", position)), is(1));
        assertThat(binlogPosition.compareTo(new BinlogPosition("mysql-bin.000003", position)), is(-1));
        String fileName = "mysql-bin.000001";
        binlogPosition = new BinlogPosition(fileName, 10);
        assertThat(binlogPosition.compareTo(new BinlogPosition(fileName, 10)), is(0));
        assertThat(binlogPosition.compareTo(new BinlogPosition(fileName, 9)), is(1));
        assertThat(binlogPosition.compareTo(new BinlogPosition(fileName, 11)), is(-1));
        assertThat(binlogPosition.compareTo(null), is(1));
    }
    
    @Test
    public void assertToJson() {
        BinlogPosition binlogPosition = new BinlogPosition("mysql-bin.000001", 4);
        assertThat(GSON.toJson(binlogPosition), is("{\"filename\":\"mysql-bin.000001\",\"position\":4,\"delay\":0}"));
    }
}
