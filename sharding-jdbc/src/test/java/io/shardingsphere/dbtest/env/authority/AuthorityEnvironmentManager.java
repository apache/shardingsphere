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

package io.shardingsphere.dbtest.env.authority;

import io.shardingsphere.dbtest.cases.authority.Authority;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Authority environment manager.
 *
 * @author panjuan
 */
public final class AuthorityEnvironmentManager {
    
    private final Authority authority;
    
    private final DataSource dataSource;
    
    public AuthorityEnvironmentManager(final String path, final DataSource dataSource) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(path)) {
            authority = (Authority) JAXBContext.newInstance(Authority.class).createUnmarshaller().unmarshal(reader);
        }
       this.dataSource = dataSource;
    }
    
    /**
     * Initialize data.
     * 
     * @throws SQLException SQL exception
     * @throws ParseException parse exception
     */
    public void initialize() throws SQLException, ParseException {
    
    
    }
}
