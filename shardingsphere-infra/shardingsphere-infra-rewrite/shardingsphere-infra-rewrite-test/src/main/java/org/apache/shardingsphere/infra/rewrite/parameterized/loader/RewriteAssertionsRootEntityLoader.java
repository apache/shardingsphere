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

package org.apache.shardingsphere.infra.rewrite.parameterized.loader;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.rewrite.parameterized.entity.RewriteAssertionsRootEntity;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;

/**
 * Rewrite assertions root entity loader for JAXB.
 */
public final class RewriteAssertionsRootEntityLoader {
    
    /**
     * Load rewrite assertions from XML.
     *
     * @param file rewrite assertions file
     * @return rewrite assertions entity for JAXB
     */
    @SneakyThrows(JAXBException.class)
    public RewriteAssertionsRootEntity load(final String file) {
        InputStream inputStream = RewriteAssertionsRootEntityLoader.class.getClassLoader().getResourceAsStream(file);
        return null == inputStream ? new RewriteAssertionsRootEntity()
                : (RewriteAssertionsRootEntity) JAXBContext.newInstance(RewriteAssertionsRootEntity.class).createUnmarshaller().unmarshal(inputStream);
    }
}
