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

package org.apache.shardingsphere.proxy.backend.response.data;

import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.impl.TextQueryResponseCell;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryResponseRowTest {

    @Test
    public void assertGetDataWhenQueryResponseCellsOfEmptyList() {
        QueryResponseRow queryResponseRow = new QueryResponseRow(Collections.emptyList());
        assertThat(queryResponseRow.getData(), is(Collections.emptyList()));
    }

    @Test
    public void assertGetDataWhenQueryResponseCellsInstanceOfBinaryQueryResponseCell() {
        BinaryQueryResponseCell binaryQueryResponseCell1 = mock(BinaryQueryResponseCell.class);
        when(binaryQueryResponseCell1.getData()).thenReturn(1);
        BinaryQueryResponseCell binaryQueryResponseCell2 = mock(BinaryQueryResponseCell.class);
        when(binaryQueryResponseCell2.getData()).thenReturn("2");
        QueryResponseRow queryResponseRow = new QueryResponseRow(Arrays.asList(binaryQueryResponseCell1, binaryQueryResponseCell2));
        assertThat(queryResponseRow.getData().size(), is(2));
        assertTrue(queryResponseRow.getData().containsAll(Arrays.asList(1, "2")));
    }

    @Test
    public void assertGetDataWhenQueryResponseCellsInstanceOfTextQueryResponseCell() {
        TextQueryResponseCell textQueryResponseCell1 = mock(TextQueryResponseCell.class);
        when(textQueryResponseCell1.getData()).thenReturn("column_1");
        TextQueryResponseCell textQueryResponseCell2 = mock(TextQueryResponseCell.class);
        when(textQueryResponseCell2.getData()).thenReturn("column_2");
        QueryResponseRow queryResponseRow = new QueryResponseRow(Arrays.asList(textQueryResponseCell1, textQueryResponseCell2));
        assertThat(queryResponseRow.getData().size(), is(2));
        assertThat(queryResponseRow.getData().iterator().next(), instanceOf(String.class));
        assertTrue(queryResponseRow.getData().containsAll(Arrays.asList("column_1", "column_2")));
    }

    @Test
    public void assertGetDataWhenQueryResponseCellsInstanceOfAllTypeQueryResponseCell() {
        BinaryQueryResponseCell binaryQueryResponseCell = mock(BinaryQueryResponseCell.class);
        when(binaryQueryResponseCell.getData()).thenReturn(1);
        TextQueryResponseCell textQueryResponseCell = mock(TextQueryResponseCell.class);
        when(textQueryResponseCell.getData()).thenReturn("column");
        QueryResponseRow queryResponseRow = new QueryResponseRow(Arrays.asList(binaryQueryResponseCell, textQueryResponseCell));
        assertThat(queryResponseRow.getData().size(), is(2));
        assertTrue(queryResponseRow.getData().containsAll(Arrays.asList(1, "column")));
    }
}
