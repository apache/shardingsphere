/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.spring.cases;

import io.shardingsphere.shardingjdbc.spring.AbstractShardingBothDataBasesAndTablesJUnitTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceForMasterSlaveWithStrategyType.xml")
public final class WithNamespaceForMasterSlaveWithStrategyTypeTest extends AbstractShardingBothDataBasesAndTablesJUnitTest {
    
    @Override
    protected List<String> getSchemaFiles() {
        return Arrays.asList("schema/dbtbl_0_master.sql", "schema/dbtbl_0_slave_0.sql", "schema/dbtbl_0_slave_1.sql",
                "schema/dbtbl_1_master.sql", "schema/dbtbl_1_slave_0.sql", "schema/dbtbl_1_slave_1.sql");
    }
}
