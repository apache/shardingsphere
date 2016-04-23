/**
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

package com.dangdang.ddframe.rdb.sharding.router.mixed;

import com.dangdang.ddframe.rdb.sharding.router.single.SingleRoutingTableFactor;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CartesianResultTest {
    
    @Test
    public void assertToString() {
        CartesianResult actual = new CartesianResult();
        CartesianTableReference tableReference = new CartesianTableReference(Collections.singletonList(new SingleRoutingTableFactor("logic", "actual")));
        CartesianDataSource dataSource = new CartesianDataSource("ds", tableReference);
        actual.getRoutingDataSources().add(dataSource);
        assertThat(actual.toString(), is("CartesianResult(routingDataSources=[CartesianDataSource(dataSource=ds, "
                + "routingTableReferences=[CartesianTableReference(routingTableFactors=[SingleRoutingTableFactor(logicTable=logic, actualTable=actual)])])])"));
    }
}
