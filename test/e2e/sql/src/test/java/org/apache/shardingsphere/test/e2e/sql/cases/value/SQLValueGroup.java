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

package org.apache.shardingsphere.test.e2e.sql.cases.value;

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Group of SQL value.
 */
@Getter
public final class SQLValueGroup {
    
    private final Collection<SQLValue> values;
    
    public SQLValueGroup(final DataSetMetaData metaData, final List<String> values) {
        this.values = createSQLValues(metaData, values);
    }
    
    private Collection<SQLValue> createSQLValues(final DataSetMetaData metaData, final List<String> values) {
        Collection<SQLValue> result = new LinkedList<>();
        int count = 0;
        for (String each : values) {
            result.add(new SQLValue(each, metaData.getColumns().get(count).getType(), count + 1));
            count++;
        }
        return result;
    }
}
