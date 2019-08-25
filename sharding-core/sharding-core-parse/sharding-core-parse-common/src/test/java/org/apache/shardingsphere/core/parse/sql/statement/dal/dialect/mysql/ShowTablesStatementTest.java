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

package org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ShowTablesStatementTest {
    
    @Test
    public void assertSetAndGetFull() {
        boolean full = false;
        ShowTablesStatement showTablesStatement = new ShowTablesStatement();
        showTablesStatement.setFull(full);
        assertThat(showTablesStatement.isFull(), is(full));
    }
    
    @Test
    public void assertSetAndGetSchema() {
        String schema = "sharding_db";
        ShowTablesStatement showTablesStatement = new ShowTablesStatement();
        showTablesStatement.setSchema(schema);
        assertThat(showTablesStatement.getSchema(), is(schema));
    }
    
    @Test
    public void assertSetAndGetPattern() {
        String pattern = "t_order%";
        ShowTablesStatement showTablesStatement = new ShowTablesStatement();
        showTablesStatement.setPattern(pattern);
        assertThat(showTablesStatement.getPattern(), is(pattern));
    }
}
