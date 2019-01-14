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

package io.shardingsphere.transaction.saga.servicecomb.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;

public class SagaDefinitionBuilderTest {
    
    private static final String DS_0 = "ds_0";
    
    private static final String DS_1 = "ds_1";
    
    private static final String EXAMPLE_INSERT_SQL = "INSERT INTO TABLE ds_0.tb_0 (id, value) VALUES (?, ?)";
    
    private static final String EXAMPLE_DELETE_SQL = "DELETE FROM ds_0.tb_0 WHERE id=?";
    
    private static final String EXAMPLE_UPDATE_SQL = "UPDATE ds_1.tb_1 SET value=? where id=?";
    
    private SagaDefinitionBuilder builder;
    
    private static final List<List<Object>> INSERT_PARAMS = new ArrayList<List<Object>>() {{
        add(new ArrayList<Object>() {{
            add(1);
            add("xxx");
        }});
    }};
    private static final List<Collection<Object>> DELETE_PARAMS = new ArrayList<Collection<Object>>() {{
        add(new ArrayList<Object>() {{
            add(1);
        }});
    }};
    private static final List<List<Object>> UPDATE_PARAMS = new ArrayList<List<Object>>() {{
        add(new ArrayList<Object>() {{
            add("yyy");
            add(2);
        }});
    }};
    private static final List<Collection<Object>> UPDATE_C_PARAMS = new ArrayList<Collection<Object>>() {{
        add(new ArrayList<Object>() {{
            add("xxx");
            add(2);
        }});
    }};
    
    private static final String EXPECT_EMPTY_SQL_DEFINITION = "{\"policy\":\"ForwardRecovery\",\"requests\":[]}";
    private static final String EXPECT_SINGLE_SQL_DEFINITION = "{\"policy\":\"ForwardRecovery\",\"requests\":[{\"id\":\"1\",\"datasource\":\"ds_0\",\"type\":\"sql\",\"transaction\":{\"sql\":\"INSERT INTO TABLE ds_0.tb_0 (id, value) VALUES (?, ?)\",\"params\":[[1,\"xxx\"]],\"retries\":5},\"compensation\":{\"sql\":\"DELETE FROM ds_0.tb_0 WHERE id=?\",\"params\":[[1]],\"retries\":5},\"parents\":[],\"failRetryDelayMilliseconds\":5000}]}";
    private static final String EXPECT_DOUBLE_SQL_DEFINITION = "{\"policy\":\"ForwardRecovery\",\"requests\":[{\"id\":\"1\",\"datasource\":\"ds_0\",\"type\":\"sql\",\"transaction\":{\"sql\":\"INSERT INTO TABLE ds_0.tb_0 (id, value) VALUES (?, ?)\",\"params\":[[1,\"xxx\"]],\"retries\":5},\"compensation\":{\"sql\":\"DELETE FROM ds_0.tb_0 WHERE id=?\",\"params\":[[1]],\"retries\":5},\"parents\":[],\"failRetryDelayMilliseconds\":5000},{\"id\":\"2\",\"datasource\":\"ds_1\",\"type\":\"sql\",\"transaction\":{\"sql\":\"UPDATE ds_1.tb_1 SET value=? where id=?\",\"params\":[[\"yyy\",2]],\"retries\":5},\"compensation\":{\"sql\":\"UPDATE ds_1.tb_1 SET value=? where id=?\",\"params\":[[\"xxx\",2]],\"retries\":5},\"parents\":[],\"failRetryDelayMilliseconds\":5000}]}";
    private static final String EXPECT_PARENTS_SQL_DEFINITION = "{\"policy\":\"ForwardRecovery\",\"requests\":[{\"id\":\"1\",\"datasource\":\"ds_0\",\"type\":\"sql\",\"transaction\":{\"sql\":\"INSERT INTO TABLE ds_0.tb_0 (id, value) VALUES (?, ?)\",\"params\":[[1,\"xxx\"]],\"retries\":5},\"compensation\":{\"sql\":\"DELETE FROM ds_0.tb_0 WHERE id=?\",\"params\":[[1]],\"retries\":5},\"parents\":[],\"failRetryDelayMilliseconds\":5000},{\"id\":\"2\",\"datasource\":\"ds_1\",\"type\":\"sql\",\"transaction\":{\"sql\":\"UPDATE ds_1.tb_1 SET value=? where id=?\",\"params\":[[\"yyy\",2]],\"retries\":5},\"compensation\":{\"sql\":\"UPDATE ds_1.tb_1 SET value=? where id=?\",\"params\":[[\"xxx\",2]],\"retries\":5},\"parents\":[\"1\"],\"failRetryDelayMilliseconds\":5000}]}";
    
    @Before
    public void setUp() {
        builder = new SagaDefinitionBuilder("ForwardRecovery", 5, 5, 5000);
    }
    
    @Test
    public void assertBuildEmpty() throws JsonProcessingException {
        assertThat(builder.build(), is(EXPECT_EMPTY_SQL_DEFINITION));
    }
    
    @Test
    public void assertAddChildRequestAndBuild() throws JsonProcessingException {
        builder.addChildRequest("1", "ds_0", EXAMPLE_INSERT_SQL, INSERT_PARAMS, EXAMPLE_DELETE_SQL, DELETE_PARAMS);
        assertThat(builder.build(), is(EXPECT_SINGLE_SQL_DEFINITION));
        builder.addChildRequest("2", "ds_1", EXAMPLE_UPDATE_SQL, UPDATE_PARAMS, EXAMPLE_UPDATE_SQL, UPDATE_C_PARAMS);
        assertThat(builder.build(), is(EXPECT_DOUBLE_SQL_DEFINITION));
    }
    
    @Test
    public void assertSwitchParents() throws JsonProcessingException {
        builder.addChildRequest("1", "ds_0", EXAMPLE_INSERT_SQL, INSERT_PARAMS, EXAMPLE_DELETE_SQL, DELETE_PARAMS);
        builder.switchParents();
        builder.addChildRequest("2", "ds_1", EXAMPLE_UPDATE_SQL, UPDATE_PARAMS, EXAMPLE_UPDATE_SQL, UPDATE_C_PARAMS);
        assertThat(builder.build(), is(EXPECT_PARENTS_SQL_DEFINITION));
    }
    
}
