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

package org.apache.shardingsphere.mode.repository.standalone.h2;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepositoryProperties;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepositoryPropertyKey;

import java.util.Optional;
import java.util.Properties;

/**
 * H2 repository.
 */
@Slf4j
public final class H2Repository extends JDBCRepository {
    
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL";
    
    private static final String DEFAULT_USER = "sa";
    
    private static final String DEFAULT_PASSWORD = "";
    
    @Override
    public void init(final Properties props) {
        JDBCRepositoryProperties localRepositoryProps = new JDBCRepositoryProperties(props);
        String jdbcUrl = Optional.ofNullable(Strings.emptyToNull(localRepositoryProps.getValue(JDBCRepositoryPropertyKey.JDBC_URL))).orElse(DEFAULT_JDBC_URL);
        String user = Optional.ofNullable(Strings.emptyToNull(localRepositoryProps.getValue(JDBCRepositoryPropertyKey.USER))).orElse(DEFAULT_USER);
        String password = Optional.ofNullable(Strings.emptyToNull(localRepositoryProps.getValue(JDBCRepositoryPropertyKey.PASSWORD))).orElse(DEFAULT_PASSWORD);
        initTable(jdbcUrl, user, password);
    }
    
    @Override
    public String get(final String key) {
        return Optional.ofNullable(Strings.emptyToNull(super.get(key))).map(each -> each.replace("\"", "'")).orElse("");
    }
    
    @Override
    public void persist(final String key, final String value) {
        // Single quotation marks are the keywords executed by H2. Replace with double quotation marks.
        String insensitiveValue = value.replace("'", "\"");
        super.persist(key, insensitiveValue);
    }
    
    @Override
    public String getType() {
        return "H2";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
