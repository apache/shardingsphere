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

package info.avalon566.shardingscaling.sync.jdbc;

import info.avalon566.shardingscaling.sync.core.Record;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
@Data
public class DataRecord implements Record {
    private String type;
    private String tableName;
    private String fullTableName;
    private final List<Column> columns;

    public DataRecord(int columnCount) {
        columns = new ArrayList<Column>(columnCount);
    }

    public void addColumn(Column data) {
        columns.add(data);
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public String getTableName() {
        return fullTableName.split("\\.")[1];
    }
}