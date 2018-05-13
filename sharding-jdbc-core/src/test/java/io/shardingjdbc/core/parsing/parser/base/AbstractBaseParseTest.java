/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.parser.base;

import io.shardingjdbc.core.common.jaxb.helper.SQLStatementHelper;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.parser.jaxb.Assert;
import io.shardingjdbc.core.parsing.parser.jaxb.Asserts;
import lombok.AccessLevel;
import lombok.Getter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractBaseParseTest {
    
    @Getter(AccessLevel.PROTECTED)
    private final String testCaseName;
    
    @Getter(AccessLevel.PROTECTED)
    private final DatabaseType databaseType;
    
    @Getter(AccessLevel.PROTECTED)
    private final Assert assertObj;
    
    AbstractBaseParseTest(
            final String testCaseName, final DatabaseType databaseType, final Assert assertObj) {
        this.testCaseName = testCaseName;
        this.databaseType = databaseType;
        this.assertObj = assertObj;
    }
    
    protected static Collection<Object[]> dataParameters() {
        Collection<Object[]> result = new ArrayList<>();
        URL url = AbstractBaseParseTest.class.getClassLoader().getResource("parser/");
        if (null == url) {
            return result;
        }
        File[] files = new File(url.getPath()).listFiles();
        if (null == files) {
            return result;
        }
        for (File each : files) {
            result.addAll(dataParameters(each));
        }
        return result;
    }
    
    private static Collection<Object[]> dataParameters(final File file) {
        Asserts asserts = loadAsserts(file);
        List<Object[]> result = new ArrayList<>();
        for (int i = 0; i < asserts.getAsserts().size(); i++) {
            Assert assertObj = asserts.getAsserts().get(i);
            for (DatabaseType each : SQLStatementHelper.getTypes(assertObj.getId())) {
                result.add(getDataParameter(assertObj, each));
            }
        }
        return result;
    }
    
    private static Asserts loadAsserts(final File file) {
        try {
            return (Asserts) JAXBContext.newInstance(Asserts.class).createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Object[] getDataParameter(final Assert assertObj, final DatabaseType dbType) {
        final Object[] result = new Object[3];
        result[0] = assertObj.getId();
        result[1] = dbType;
        result[2] = assertObj;
        return result;
    }
}
