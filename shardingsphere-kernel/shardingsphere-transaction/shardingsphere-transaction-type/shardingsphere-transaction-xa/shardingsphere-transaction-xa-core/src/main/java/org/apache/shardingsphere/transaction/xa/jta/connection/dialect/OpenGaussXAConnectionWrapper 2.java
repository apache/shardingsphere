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

package org.apache.shardingsphere.transaction.xa.jta.connection.dialect;

import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * OpenGauss XA connection wrapper.
 */
public final class OpenGaussXAConnectionWrapper implements XAConnectionWrapper {
    
    private static final String BASE_CONNECTION_CLASS = "org.opengauss.core.BaseConnection";
    
    private static final String PG_XA_CONNECTION_CLASS = "org.opengauss.xa.PGXAConnection";
    
    @SneakyThrows({SQLException.class, ClassNotFoundException.class, NoSuchMethodException.class, SecurityException.class,
            InstantiationException.class, IllegalAccessException.class, InvocationTargetException.class})
    @Override
    public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) {
        Class<?> baseConnectionClass = Class.forName(BASE_CONNECTION_CLASS);
        Object physicalConnection = connection.unwrap(baseConnectionClass);
        Constructor<?> pgXAConnectionConstructor = Class.forName(PG_XA_CONNECTION_CLASS).getConstructor(baseConnectionClass);
        return (XAConnection) pgXAConnectionConstructor.newInstance(physicalConnection);
    }
}
