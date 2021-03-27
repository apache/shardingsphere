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

package org.apache.shardingsphere.infra.executor.exec.sql;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.Util;

public final class ShardingSqlImplementor extends RelToSqlConverter {
    /**
     * Creates a RelToSqlConverter.
     *
     * @param dialect sql dialect, see {@link SqlDialect}
     */
    public ShardingSqlImplementor(final SqlDialect dialect) {
        super(dialect);
    }
    
    @Override
    public Result visit(final TableScan e) {
        String tableName = Util.last(e.getTable().getQualifiedName());
        SqlIdentifier sqlNode = new SqlIdentifier(ImmutableList.of(tableName), SqlParserPos.ZERO);
        return result(sqlNode, ImmutableList.of(Clause.FROM), e, null);
    }
}
