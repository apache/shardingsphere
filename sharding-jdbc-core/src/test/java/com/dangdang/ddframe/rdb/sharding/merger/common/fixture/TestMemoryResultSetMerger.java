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

package com.dangdang.ddframe.rdb.sharding.merger.common.fixture;

import com.dangdang.ddframe.rdb.sharding.merger.common.AbstractMemoryResultSetMerger;

import java.sql.SQLException;
import java.util.Map;

public final class TestMemoryResultSetMerger extends AbstractMemoryResultSetMerger {
    
    public TestMemoryResultSetMerger(final Map<String, Integer> labelAndIndexMap) {
        super(labelAndIndexMap);
    }
    
    @Override
    public boolean next() throws SQLException {
        return false;
    }
}
