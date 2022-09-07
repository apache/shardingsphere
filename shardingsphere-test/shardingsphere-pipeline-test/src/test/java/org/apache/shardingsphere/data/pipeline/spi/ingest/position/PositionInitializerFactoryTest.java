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

package org.apache.shardingsphere.data.pipeline.spi.ingest.position;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.DefaultPositionInitializer;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLPositionInitializer;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.OpenGaussPositionInitializer;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLPositionInitializer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class PositionInitializerFactoryTest {
    
    @Test
    public void assertGetInstanceForMySQL() {
        assertThat(PositionInitializerFactory.getInstance("MySQL"), instanceOf(MySQLPositionInitializer.class));
    }
    
    @Test
    public void assertGetInstanceForPostgreSQL() {
        assertThat(PositionInitializerFactory.getInstance("PostgreSQL"), instanceOf(PostgreSQLPositionInitializer.class));
    }
    
    @Test
    public void assertGetInstanceForOpenGauss() {
        assertThat(PositionInitializerFactory.getInstance("openGauss"), instanceOf(OpenGaussPositionInitializer.class));
    }
    
    @Test
    public void assertGetInstanceForOracle() {
        assertThat(PositionInitializerFactory.getInstance("Oracle"), instanceOf(DefaultPositionInitializer.class));
    }
}
