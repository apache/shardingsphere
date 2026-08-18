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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.binary.BinaryRow;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class FirebirdBinaryRowBuilderTest {
    
    @Test
    void assertBuildPreservesCellOrderAndData() {
        byte[] expectedData = new byte[]{1, 2};
        QueryResponseRow row = new QueryResponseRow(Arrays.asList(new QueryResponseCell(Types.BLOB, expectedData), new QueryResponseCell(Types.INTEGER, null)));
        BinaryRow actual = FirebirdBinaryRowBuilder.build(row);
        assertThat(actual.getCells().size(), is(2));
        Iterator<BinaryCell> iterator = actual.getCells().iterator();
        BinaryCell firstCell = iterator.next();
        assertThat(firstCell.getColumnType(), is(FirebirdBinaryColumnType.BLOB));
        assertThat(firstCell.getData(), sameInstance(expectedData));
        BinaryCell secondCell = iterator.next();
        assertThat(secondCell.getColumnType(), is(FirebirdBinaryColumnType.LONG));
        assertNull(secondCell.getData());
        assertFalse(iterator.hasNext());
    }
}
