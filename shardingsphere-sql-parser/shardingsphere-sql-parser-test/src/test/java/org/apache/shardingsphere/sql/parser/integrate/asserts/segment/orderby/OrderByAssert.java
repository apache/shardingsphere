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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.orderby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedOrderBy;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

/**
 * Order by assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderByAssert {
    
    /**
     * Assert actual order by segment is correct with expected order by.
     *
     * @param assertMessage assert message
     * @param actual actual order by segment
     * @param expected expected order by
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLCaseAssertMessage assertMessage, final OrderBySegment actual, final ExpectedOrderBy expected, final SQLCaseType sqlCaseType) {
        OrderByItemAssert.assertIs(assertMessage, actual.getOrderByItems(), expected, sqlCaseType, "Order by");
    }
}
