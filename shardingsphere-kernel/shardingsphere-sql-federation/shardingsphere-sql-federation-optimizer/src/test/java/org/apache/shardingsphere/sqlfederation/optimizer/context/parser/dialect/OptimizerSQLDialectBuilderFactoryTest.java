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

package org.apache.shardingsphere.sqlfederation.optimizer.context.parser.dialect;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.dialect.impl.H2OptimizerBuilder;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.dialect.impl.MySQLOptimizerBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OptimizerSQLDialectBuilderFactoryTest {
    
    @Test
    public void assertBuildWithDatabaseType() {
        DatabaseType databaseType = DatabaseTypeFactory.getInstance("H2");
        assertThat(OptimizerSQLDialectBuilderFactory.build(databaseType), is(new H2OptimizerBuilder().build()));
    }
    
    @Test
    public void assertBuildWithoutDatabaseType() {
        assertThat(OptimizerSQLDialectBuilderFactory.build(null), is(new MySQLOptimizerBuilder().build()));
    }
}
