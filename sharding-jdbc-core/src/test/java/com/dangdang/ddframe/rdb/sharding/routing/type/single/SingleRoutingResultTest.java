/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.routing.type.single;

import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SingleRoutingResultTest {
    
    @Test
    @Ignore
    public void assertToString() {
        SingleRoutingResult actual = new SingleRoutingResult();
        SingleRoutingDataSource dataSource = new SingleRoutingDataSource("ds", new TableUnit("logic", "actual"));
        actual.getRoutingDataSources().add(dataSource);
        assertThat(actual.toString(), is("SingleRoutingResult(routingDataSources=["
                + "SingleRoutingDataSource(dataSource=ds, routingTableFactors=[SingleRoutingTableFactor(logicTable=logic, actualTable=actual)])])"));
    }
}
