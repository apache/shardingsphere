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

package org.apache.shardingsphere.test.it.data.pipeline.core.fixture;

import org.apache.shardingsphere.data.pipeline.core.exception.syntax.CreateTableSQLGenerateException;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGenerator;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

/**
* Create table SQL generator for H2.
 */
public final class H2CreateTableSQLGenerator implements CreateTableSQLGenerator {
    
    @Override
    public Collection<String> generate(final DataSource dataSource, final String schemaName, final String tableName) {
        if ("t_order".equalsIgnoreCase(tableName)) {
            return Collections.singletonList(PipelineContextUtils.getCreateOrderTableSchema());
        }
        throw new CreateTableSQLGenerateException(tableName);
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
