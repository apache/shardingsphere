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

package org.apache.shardingsphere.mode.persist.service.divided;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

/**
 * Persist service builder.
 */
public interface PersistServiceBuilder extends TypedSPI {
    
    /**
     * Build meta data manager persist service.
     *
     * @param repository persist repository
     * @param metaDataContextManager meta data context manager
     * @return meta data manager persist service
     */
    MetaDataManagerPersistService buildMetaDataManagerPersistService(PersistRepository repository, MetaDataContextManager metaDataContextManager);
    
    /**
     * Build process persist service.
     *
     * @param repository persist repository
     * @return process persist service
     */
    ProcessPersistService buildProcessPersistService(PersistRepository repository);
}
