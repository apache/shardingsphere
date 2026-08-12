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

package org.apache.shardingsphere.database.connector.mariadb.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.AbstractDelegatingDialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DDLCommitPolicy;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.MySQLDatabaseMetaData;

import java.util.Collections;

/**
 * Database meta data of MariaDB.
 */
public final class MariaDBDatabaseMetaData extends AbstractDelegatingDialectDatabaseMetaData {
    
    public MariaDBDatabaseMetaData() {
        super(new MySQLDatabaseMetaData());
    }
    
    @Override
    public DialectTransactionOption getTransactionOption() {
        return new DialectTransactionOption(false, DDLCommitPolicy.NO_ADDITIONAL_COMMIT, true, false, true, false, false,
                Collections.singleton("org.mariadb.jdbc.MariaDbDataSource"));
    }
    
    @Override
    public String getDatabaseType() {
        return "MariaDB";
    }
}
