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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class OrderByTokenTest {
    
    private OrderByToken orderByToken;
    
    @Before
    public void setup() {
        orderByToken = new OrderByToken(0);
        List<String> columnLabels = orderByToken.getColumnLabels();
        List<OrderDirection> orderDirections = orderByToken.getOrderDirections();
        columnLabels.add(0, "Test1");
        columnLabels.add(1, "Test2");
        orderDirections.add(0, OrderDirection.ASC);
        orderDirections.add(1, OrderDirection.ASC);
    }
    
    @Test
    public void assertToString() {
        assertThat(orderByToken.toString(), is(" ORDER BY Test1 ASC,Test2 ASC "));
    }
}
