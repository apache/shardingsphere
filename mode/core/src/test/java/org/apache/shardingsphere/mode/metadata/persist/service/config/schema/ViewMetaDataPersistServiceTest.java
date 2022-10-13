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

package org.apache.shardingsphere.mode.metadata.persist.service.config.schema;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.ViewMetaDataPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ViewMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertPersist() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "select `db`.`db`.`id` AS `id`,`db`.`db`.`order_id` AS `order_id` from `db`.`db`");
        new ViewMetaDataPersistService(repository).persist("foo_db", "foo_schema", Collections.singletonMap("foo_view", view));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/views/foo_view", "name: foo_view" + System.lineSeparator()
                + "viewDefinition: select `db`.`db`.`id` AS `id`,`db`.`db`.`order_id` AS `order_id` from" + System.lineSeparator()
                + "  `db`.`db`" + System.lineSeparator());
    }
    
    @Test
    public void assertLoad() {
        ViewMetaDataPersistService viewMetaDataPersistService = new ViewMetaDataPersistService(repository);
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/views")).thenReturn(Collections.singletonList("foo_view"));
        when(repository.getDirectly("/metadata/foo_db/schemas/foo_schema/views/foo_view")).thenReturn(readYAML());
        Map<String, ShardingSphereView> views = viewMetaDataPersistService.load("foo_db", "foo_schema");
        assertThat(views.size(), is(1));
        assertThat(views.get("foo_view").getName(), is("foo_view"));
        assertThat(views.get("foo_view").getViewDefinition(), is("select `db`.`db`.`id` AS `id`,`db`.`db`.`order_id` AS `order_id` from `db`.`db`"));
    }
    
    @Test
    public void assertDelete() {
        new ViewMetaDataPersistService(repository).delete("foo_db", "foo_schema", "foo_view");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema/views/foo_view");
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/schema/view.yaml").toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
