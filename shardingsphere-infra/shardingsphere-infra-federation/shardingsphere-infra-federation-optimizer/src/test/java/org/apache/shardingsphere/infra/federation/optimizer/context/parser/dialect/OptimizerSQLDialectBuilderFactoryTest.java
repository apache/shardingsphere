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

package org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect;

import java.util.Properties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.fixture.OptimizerSQLDialectBuilderFixture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public final class OptimizerSQLDialectBuilderFactoryTest {
    
    @Test
    public void assertCreateOptimizerSQLDialectBuilder() {
        DatabaseType type = DatabaseTypeFactory.getInstance("FIXTURE");
        Properties actual = OptimizerSQLDialectBuilderFactory.build(type);
        OptimizerSQLDialectBuilder builder = new OptimizerSQLDialectBuilderFixture();
        Properties excepted = builder.build();
        assertThat(actual, equalTo(excepted));
    }
}
