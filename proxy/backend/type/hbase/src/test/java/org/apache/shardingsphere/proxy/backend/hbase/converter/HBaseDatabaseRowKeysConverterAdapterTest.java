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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HBaseDatabaseRowKeysConverterAdapterTest {
    
    @Test
    public void assertGetRowKeysFromInExpression() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey in ('1', '2') ");
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        HBaseDatabaseRowKeysConverterAdapter adapter = new HBaseDatabaseRowKeysConverterAdapter();
        if (whereSegment.isPresent()) {
            List<String> rowKeys = adapter.getRowKeysFromWhereSegmentByIn((InExpression) whereSegment.get().getExpr());
            List<String> actual = Arrays.asList("1", "2");
            assertEquals(rowKeys, actual);
        } else {
            fail();
        }
    }
}
