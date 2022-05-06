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

package org.apache.shardingsphere.example.proxy.distsql.feature;

import org.apache.shardingsphere.example.proxy.distsql.DistSQLExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractFeatureExecutor implements DistSQLExecutor {
    
    protected Statement statement;
    
    protected void executeUseSchema() throws SQLException {
        statement.execute("use `example_db`");
    }
    
    protected List<List<String>> getResultData(ResultSet resultSet) throws SQLException {
        List<List<String>> result = new LinkedList<>();
        while (resultSet.next()) {
            List<String> row = new LinkedList<>();
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                row.add(resultSet.getString(i + 1));
            }
            result.add(row);
        }
        return result;
    }
}
