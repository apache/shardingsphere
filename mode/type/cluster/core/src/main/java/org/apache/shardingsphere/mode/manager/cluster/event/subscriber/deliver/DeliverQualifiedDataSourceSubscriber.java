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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.deliver;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.event.deliver.datasource.qualified.QualifiedDataSourceDeletedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.metadata.persist.node.QualifiedDataSourceNode;

/**
 * Deliver data source status subscriber.
 */
@RequiredArgsConstructor
public final class DeliverQualifiedDataSourceSubscriber implements EventSubscriber {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Delete qualified data source.
     *
     * @param event qualified data source deleted event
     */
    @Subscribe
    public void delete(final QualifiedDataSourceDeletedEvent event) {
        repository.delete(QualifiedDataSourceNode.getQualifiedDataSourceNodePath(event.getQualifiedDataSource()));
    }
}
