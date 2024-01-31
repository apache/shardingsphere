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

package org.apache.shardingsphere.test.e2e.engine.composer;

import org.apache.shardingsphere.test.e2e.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSet;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.framework.param.model.CaseTestParameter;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Batch E2E container composer.
 */
public final class BatchE2EContainerComposer extends E2EContainerComposer implements AutoCloseable {
    
    private final Collection<DataSet> dataSets = new LinkedList<>();
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public BatchE2EContainerComposer(final CaseTestParameter testParam) throws JAXBException, IOException {
        super(testParam);
        for (IntegrationTestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
            dataSets.add(DataSetLoader.load(testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), each.getExpectedDataFile()));
        }
        dataSetEnvironmentManager = new DataSetEnvironmentManager(new ScenarioDataPath(testParam.getScenario()).getDataSetFile(Type.ACTUAL), getActualDataSourceMap());
        dataSetEnvironmentManager.fillData();
    }
    
    /**
     * Get data set.
     * 
     * @param actualUpdateCounts actual update counts
     * @return data set
     */
    public DataSet getDataSet(final int[] actualUpdateCounts) {
        Collection<DataSet> dataSets = new LinkedList<>();
        assertThat(actualUpdateCounts.length, is(this.dataSets.size()));
        int count = 0;
        for (DataSet each : this.dataSets) {
            if (Statement.SUCCESS_NO_INFO != actualUpdateCounts[count]) {
                assertThat(actualUpdateCounts[count], is(each.getUpdateCount()));
            }
            dataSets.add(each);
            count++;
        }
        return mergeDataSets(dataSets);
    }
    
    private DataSet mergeDataSets(final Collection<DataSet> dataSets) {
        DataSet result = new DataSet();
        Set<DataSetRow> existedRows = new HashSet<>();
        for (DataSet each : dataSets) {
            mergeMetaData(each, result);
            mergeRow(each, result, existedRows);
        }
        sortRow(result);
        return result;
    }
    
    private void mergeMetaData(final DataSet original, final DataSet dist) {
        if (dist.getMetaDataList().isEmpty()) {
            dist.getMetaDataList().addAll(original.getMetaDataList());
        }
    }
    
    private void mergeRow(final DataSet original, final DataSet dist, final Set<DataSetRow> existedRows) {
        for (DataSetRow each : original.getRows()) {
            if (existedRows.add(each)) {
                dist.getRows().add(each);
            }
        }
    }
    
    private void sortRow(final DataSet dataSet) {
        dataSet.getRows().sort(Comparator.comparingLong(o -> Long.parseLong(o.splitValues(",").get(0))));
    }
    
    @Override
    public void close() {
        dataSetEnvironmentManager.cleanData();
    }
}
