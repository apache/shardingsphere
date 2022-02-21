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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane;

import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.Types;
import java.util.Collections;
import java.util.Optional;

/**
 * Default Sane query result engine.
 */
public final class DefaultSaneQueryResultEngine implements SaneQueryResultEngine {
    
    @Override
    public Optional<ExecuteResult> getSaneQueryResult(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement ? Optional.of(createDefaultQueryResult()) : Optional.empty();
    }
    
    private QueryResult createDefaultQueryResult() {
        RawQueryResultColumnMetaData queryResultColumnMetaData = new RawQueryResultColumnMetaData("", "", "", Types.VARCHAR, "VARCHAR", 255, 0);
        MemoryQueryResultDataRow resultDataRow = new MemoryQueryResultDataRow(Collections.singletonList("1"));
        return new RawMemoryQueryResult(new RawQueryResultMetaData(Collections.singletonList(queryResultColumnMetaData)), Collections.singletonList(resultDataRow));
    }
    
    @Override
    public String getType() {
        return "";
    }
}
