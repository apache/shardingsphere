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

package org.apache.shardingsphere.data.pipeline.spi.ddlgenerator;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class DialectDDLGeneratorFactoryTest {
    
    public static final String TYPE = "Test";
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;
    
    @Test
    public void assertFindInstanceWithDialectDDLGenerator() throws SQLException {
        
        DatabaseType databaseType = DatabaseTypeFactory.getInstance(TYPE);
        
        if (DialectDDLSQLGeneratorFactory.findInstance(databaseType).isPresent()) {
            assertThat(DialectDDLSQLGeneratorFactory.findInstance(DatabaseTypeFactory.getInstance(TYPE)).get(), is(DialectDDLGenerator.class));
        }
        
        DialectDDLGenerator dialectDDLGenerator = DialectDDLSQLGeneratorFactory.findInstance(databaseType).orElseThrow(() -> new ShardingSphereException("Failed to get dialect ddl sql generator"));
        assertThat(dialectDDLGenerator.generateDDLSQL("tableA", "", dataSource), is("SHOW CREATE TABLE tableA"));
    }
}
