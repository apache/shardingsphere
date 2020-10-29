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

package org.apache.shardingsphere.dbtest.cases.assertion.root;

import lombok.Getter;
import org.apache.shardingsphere.dbtest.cases.dataset.metadata.DataSetMetadata;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Group of SQL value.
 */
@Getter
public final class SQLValueGroup {
    
    private final Collection<SQLValue> sqlValues;
    
    public SQLValueGroup(final DataSetMetadata dataSetMetadata, final List<String> values) throws ParseException {
        sqlValues = new LinkedList<>();
        int count = 0;
        for (String each : values) {
            sqlValues.add(new SQLValue(each, dataSetMetadata.getColumns().get(count).getType(), count + 1));
            count++;
        }
    }
}
