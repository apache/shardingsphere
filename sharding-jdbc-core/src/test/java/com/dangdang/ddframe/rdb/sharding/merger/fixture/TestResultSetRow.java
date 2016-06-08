/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.fixture;

import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.ResultSetRow;

public final class TestResultSetRow implements ResultSetRow {
    
    private final Object[] dataRow;
    
    public TestResultSetRow(final Object... dataRow) {
        this.dataRow = dataRow;
    }
    
    @Override
    public void setCell(final int columnIndex, final Object value) {
        dataRow[columnIndex - 1] = value;
    }
    
    @Override
    public Object getCell(final int columnIndex) {
        return dataRow[columnIndex - 1];
    }
    
    @Override
    public boolean inRange(final int columnIndex) {
        return columnIndex > 0 && columnIndex < dataRow.length + 1;
    }
}
