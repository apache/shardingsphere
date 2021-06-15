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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AddResourceBackendHandlerTest {
    
    @Mock
    private DataSourceValidator dataSourceValidator;
    
    @Mock
    private AddResourceStatement addResourceStatement;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereResource shardingSphereResource;
    
    private AddResourceBackendHandler addResourceBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        addResourceBackendHandler = new AddResourceBackendHandler(new MySQLDatabaseType(), addResourceStatement, backendConnection);
        Field field = addResourceBackendHandler.getClass().getDeclaredField("dataSourceValidator");
        field.setAccessible(true);
        field.set(addResourceBackendHandler, dataSourceValidator);
    }
    
    @Test
    public void assertExecute() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Arrays.asList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        when(shardingSphereResource.getDataSources()).thenReturn(new HashMap<>());
        when(dataSourceValidator.validate(any(DataSourceConfiguration.class))).thenReturn(true);
        ResponseHeader responseHeader = addResourceBackendHandler.execute("test", buildAddResourceStatement());
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    private AddResourceStatement buildAddResourceStatement() {
        DataSourceSegment dataSourceSegment = new DataSourceSegment();
        dataSourceSegment.setName("ds_0");
        dataSourceSegment.setHostName("127.0.0.1");
        dataSourceSegment.setDb("test0");
        dataSourceSegment.setPort("3306");
        dataSourceSegment.setUser("root");
        Collection<DataSourceSegment> dataSources = new ArrayList<>(1);
        dataSources.add(dataSourceSegment);
        return new AddResourceStatement(dataSources);
    }
}
