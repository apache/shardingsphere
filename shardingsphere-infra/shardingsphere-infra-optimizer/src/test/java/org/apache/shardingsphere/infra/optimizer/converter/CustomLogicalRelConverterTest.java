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

package org.apache.shardingsphere.infra.optimizer.converter;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.tools.RelBuilder;
import org.apache.shardingsphere.infra.optimizer.rel.CustomLogicalRelConverter;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimizer.util.RelBuilderTest;
import org.junit.Assert;
import org.junit.Test;

public class CustomLogicalRelConverterTest {
    
    @Test
    public void testConvertTableScan() {
        RelBuilder relBuilder = RelBuilderTest.createRelBuilder();
        relBuilder.scan("EMP");
        relBuilder.filter(relBuilder.call(SqlStdOperatorTable.GREATER_THAN,
                relBuilder.field("c"),
                relBuilder.literal(10)));
        RelNode relNode = relBuilder.build();
        RelNode res = CustomLogicalRelConverter.convert(relNode);
        Assert.assertTrue(res instanceof LogicalFilter);
        Assert.assertTrue(((LogicalFilter) res).getInput() instanceof LogicalScan);
    }
}
