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

package org.apache.shardingsphere.data.pipeline.spi.fixture;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.DialectDDLGenerator;

public final class DialectDDLGeneratorFixTure implements DialectDDLGenerator {
    
    private static final String SHOW_CREATE_SQL = "SHOW CREATE TABLE %s";
    
    @Override
    public String generateDDLSQL(final String tableName, final String schemaName, final DataSource dataSource) throws SQLException {
        StringBuilder result = new StringBuilder();
        result.append(String.format(SHOW_CREATE_SQL, tableName));
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "Test";
    }
}
