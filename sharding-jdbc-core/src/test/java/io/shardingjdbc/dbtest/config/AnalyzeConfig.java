/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.config;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;

public class AnalyzeConfig {

    /**
     * Parse use case files.
     * @param path file path
     * @return AssertsDefinition
     * @throws IOException IOException
     * @throws JAXBException JAXBException
     */
    public static AssertsDefinition analyze(final String path) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(AssertsDefinition.class);

        Unmarshaller unmarshal = context.createUnmarshaller();
        FileReader reader = new FileReader(path);
        return (AssertsDefinition) unmarshal.unmarshal(reader);

    }

}
