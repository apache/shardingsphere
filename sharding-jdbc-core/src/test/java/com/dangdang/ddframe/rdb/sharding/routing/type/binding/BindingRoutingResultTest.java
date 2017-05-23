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

package com.dangdang.ddframe.rdb.sharding.routing.type.binding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingDataSource;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingTableFactor;

public final class BindingRoutingResultTest {
    
    @Test
    public void assertToString() {
        SingleRoutingResult singleRoutingResult = new SingleRoutingResult();
        SingleRoutingDataSource dataSource = new SingleRoutingDataSource("ds");
        dataSource.getRoutingTableFactors().add(new SingleRoutingTableFactor("logic", "actual"));
        singleRoutingResult.getRoutingDataSources().add(dataSource);
        BindingRoutingResult actual = new BindingRoutingResult(singleRoutingResult);
        assertThat(actual.toString(), is("BindingRoutingResult(super=SingleRoutingResult(routingDataSources=["
                + "BindingRoutingDataSource(super=SingleRoutingDataSource(dataSource=ds, routingTableFactors=["
                + "BindingRoutingTableFactor(super=SingleRoutingTableFactor(logicTable=logic, actualTable=actual), bindingRoutingTableFactors=[])]))]))"));
    }
}
