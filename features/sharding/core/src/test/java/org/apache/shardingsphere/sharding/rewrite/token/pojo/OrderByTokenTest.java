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

import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OrderByTokenTest {
    
    @Test
    void assertGetStopIndex() {
        assertThat(new OrderByToken(10).getStopIndex(), is(10));
    }
    
    @Test
    void assertToString() {
        assertThat(createOrderByToken().toString(), is(" ORDER BY foo_col ASC,bar_col ASC "));
    }
    
    private OrderByToken createOrderByToken() {
        OrderByToken result = new OrderByToken(0);
        result.getColumnLabels().add(0, "foo_col");
        result.getColumnLabels().add(1, "bar_col");
        result.getOrderDirections().add(0, OrderDirection.ASC);
        result.getOrderDirections().add(1, OrderDirection.ASC);
        return result;
    }
}
