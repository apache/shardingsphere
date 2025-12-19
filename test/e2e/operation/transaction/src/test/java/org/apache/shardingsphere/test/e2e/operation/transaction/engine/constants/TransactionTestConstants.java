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

package org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for transaction test.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionTestConstants {
    
    public static final String MYSQL = "MySQL";
    
    public static final String OPENGAUSS = "openGauss";
    
    public static final String POSTGRESQL = "PostgreSQL";
    
    public static final String ACCOUNT = "account";
    
    public static final String DEFAULT_TYPE = "default_type";
    
    public static final String PROVIDER_TYPE = "provider_type";
    
    public static final String ATOMIKOS = "Atomikos";
    
    public static final String NARAYANA = "Narayana";
    
    public static final String JDBC = "jdbc";
    
    public static final String PROXY = "proxy";
}
