package io.shardingjdbc.dbtest.asserts;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.xml.sax.SAXException;

import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.dbtest.common.ConfigRuntime;
import io.shardingjdbc.dbtest.common.DatabaseUtils;
import io.shardingjdbc.dbtest.common.FileUtils;
import io.shardingjdbc.dbtest.common.PathUtils;
import io.shardingjdbc.dbtest.config.AnalyzeConfig;
import io.shardingjdbc.dbtest.config.bean.AssertDefinition;
import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;
import io.shardingjdbc.dbtest.config.bean.parsecontext.ParseContexDefinition;
import io.shardingjdbc.dbtest.core.common.util.SQLPlaceholderUtil;
import io.shardingjdbc.dbtest.core.parsing.parser.jaxb.helper.ParserAssertHelper;
import io.shardingjdbc.dbtest.core.parsing.parser.jaxb.helper.ParserJAXBHelper;
import io.shardingjdbc.dbtest.data.AnalyzeDataset;
import io.shardingjdbc.dbtest.data.DatasetDatabase;
import io.shardingjdbc.dbtest.data.DatasetDefinition;
import io.shardingjdbc.dbtest.datasource.DataSourceUtil;
import io.shardingjdbc.dbtest.exception.DbTestException;

/**
 * 用例执行引擎
 */
public class AssertEngine {

	public static void run()
			throws IOException, SAXException, SQLException, NoSuchFieldException, IllegalAccessException,
			XPathExpressionException, ParserConfigurationException, ParseException, JAXBException {
		String assertPath = ConfigRuntime.getAssertPath();
		assertPath = PathUtils.getPath(assertPath);

		// 搜索所有用例
		List<String> paths = FileUtils.getAllFilePaths(new File(assertPath), "assert-", "xml");
		for (String path : paths) {
			runAssert(path);
		}
	}

	public static boolean runAssert(String path)
			throws IOException, SAXException, SQLException, NoSuchFieldException, IllegalAccessException,
			XPathExpressionException, ParserConfigurationException, ParseException, JAXBException {

		AssertsDefinition assertsDefinition = AnalyzeConfig.analyze(path);
		String rootPath = path.substring(0, path.lastIndexOf(File.separator) + 1);
		assertsDefinition.setPath(rootPath);

		DataSource dataSource = DataSourceUtil
				.getDataSource(PathUtils.getPath(assertsDefinition.getShardingRuleConfig(), rootPath));

		ShardingContext shardingContext = DataSourceUtil.getShardingContext((ShardingDataSource) dataSource);
		ShardingRule shardingRule = shardingContext.getShardingRule();
		Map<String, DataSource> dataSourceMaps = shardingRule.getDataSourceMap();

		List<AssertDefinition> asserts = assertsDefinition.getAsserts();
		List<String> dbs = new ArrayList<>();
		for (String s : dataSourceMaps.keySet()) {
			dbs.add(s);
		}

		for (AssertDefinition anAssert : asserts) {
			String rootsql = anAssert.getSql();

			checkParseContext(shardingContext, shardingRule, anAssert, rootsql);

			String initDataFile = PathUtils.getPath(anAssert.getInitDataFile(), rootPath);
			Map<String, DatasetDefinition> mapDatasetDefinition = new HashMap<>();
			Map<String, String> sqls = new HashMap<>();
			getInitDatas(dbs, initDataFile, mapDatasetDefinition, sqls);

			if (mapDatasetDefinition.isEmpty()) {
				throw new DbTestException(path + "  Use cases cannot be parsed");
			}

			if (sqls.isEmpty()) {
				throw new DbTestException(path + "  The use case cannot initialize the data");
			}

			if (DatabaseUtils.isSelect(rootsql)) {
				initTableData(dataSourceMaps, sqls, mapDatasetDefinition);
				try {
					try (Connection con = dataSource.getConnection();) {
						DatasetDatabase ddPreparedStatement = DatabaseUtils.selectUsePreparedStatement(con, rootsql,
								anAssert.getParameters());

						String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
						DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));

						DatabaseUtils.assertDatas(checkDataset, ddPreparedStatement);
					}

					try (Connection con = dataSource.getConnection();) {
						DatasetDatabase ddStatement = DatabaseUtils.selectUseStatement(con, rootsql,
								anAssert.getParameters());
						String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
						DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));

						DatabaseUtils.assertDatas(checkDataset, ddStatement);
					}
				} finally {
					clearTableData(dataSourceMaps, mapDatasetDefinition);
				}

			} else if(DatabaseUtils.isInsertOrUpdateOrDelete(rootsql)){
				initTableData(dataSourceMaps, sqls, mapDatasetDefinition);
				try (Connection con = dataSource.getConnection();) {
					int num = DatabaseUtils.updateUseStatementToExecuteUpdate(con, rootsql, anAssert.getParameters());
					String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
					DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));
					List<Map<String, String>> dataUpdate = checkDataset.getDatas().get("int");
					if (dataUpdate != null) {
						Assert.assertEquals(Long.valueOf(dataUpdate.get(0).get("value")).longValue(), num);
					}
				}
				clearTableData(dataSourceMaps, mapDatasetDefinition);

				initTableData(dataSourceMaps, sqls, mapDatasetDefinition);
				try (Connection con = dataSource.getConnection();) {
					boolean bool = DatabaseUtils.updateUseStatementToExecute(con, rootsql, anAssert.getParameters());
					Assert.assertTrue(bool);

				}
				clearTableData(dataSourceMaps, mapDatasetDefinition);

				initTableData(dataSourceMaps, sqls, mapDatasetDefinition);
				try (Connection con = dataSource.getConnection();) {
					int num = DatabaseUtils.updateUsePreparedStatementToExecuteUpdate(con, rootsql,
							anAssert.getParameters());
					String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
					DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));
					List<Map<String, String>> dataUpdate = checkDataset.getDatas().get("int");
					if (dataUpdate != null) {
						Assert.assertEquals(Long.valueOf(dataUpdate.get(0).get("value")).longValue(), num);
					}
				}
				clearTableData(dataSourceMaps, mapDatasetDefinition);

				initTableData(dataSourceMaps, sqls, mapDatasetDefinition);
				try (Connection con = dataSource.getConnection();) {
					boolean bool = DatabaseUtils.updateUsePreparedStatementToExecute(con, rootsql,
							anAssert.getParameters());
					Assert.assertTrue(bool);
				}
				clearTableData(dataSourceMaps, mapDatasetDefinition);

			}

		}
		return true;
	}

	private static void getInitDatas(List<String> dbs, String initDataFile, Map<String, DatasetDefinition> mapDatasetDefinition, Map<String, String> sqls) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		for (String each : dbs) {
            String tempPath = initDataFile + "/" + each + ".xml";
            File file = new File(tempPath);
            if (file.exists()) {
                DatasetDefinition datasetDefinition = AnalyzeDataset.analyze(file);
                mapDatasetDefinition.put(each, datasetDefinition);

                Map<String, List<Map<String, String>>> datas = datasetDefinition.getDatas();
                for (Map.Entry<String, List<Map<String, String>>> eachEntry : datas.entrySet()) {
                    String sql = DatabaseUtils.analyzeSql(eachEntry.getKey(),
                    		eachEntry.getValue().get(0));
                    sqls.put(eachEntry.getKey(), sql);
                }
            }
        }
	}

	private static void checkParseContext(ShardingContext shardingContext, ShardingRule shardingRule,
			AssertDefinition anAssert, String rootsql) {
		if (anAssert.getParseContex() != null) {
			ParseContexDefinition expected = anAssert.getParseContex();

			SQLStatement actual = new SQLParsingEngine(shardingContext.getDatabaseType(),
					SQLPlaceholderUtil.replacePreparedStatement(rootsql), shardingRule).parse();

			ParserAssertHelper.assertTables(expected.getTables(), actual.getTables());
			ParserAssertHelper.assertConditions(expected.getConditions(), actual.getConditions(), false);
			ParserAssertHelper.assertSqlTokens(expected.getSqlTokens(), actual.getSqlTokens(), false);
			if (actual instanceof SelectStatement) {
				SelectStatement selectStatement = (SelectStatement) actual;
				SelectStatement expectedSqlStatement = ParserJAXBHelper.getSelectStatement(expected);
				ParserAssertHelper.assertOrderBy(expectedSqlStatement.getOrderByItems(),
						selectStatement.getOrderByItems());
				ParserAssertHelper.assertGroupBy(expectedSqlStatement.getGroupByItems(),
						selectStatement.getGroupByItems());
				ParserAssertHelper.assertAggregationSelectItem(expectedSqlStatement.getAggregationSelectItems(),
						selectStatement.getAggregationSelectItems());
				ParserAssertHelper.assertLimit(expected.getLimit(), selectStatement.getLimit(), false);
			}
		}
	}

	private static void clearTableData(Map<String, DataSource> dataSourceMaps,
			Map<String, DatasetDefinition> mapDatasetDefinition) throws SQLException {
		for (Map.Entry<String, DataSource> eachEntry : dataSourceMaps.entrySet()) {

			DataSource dataSource1 = eachEntry.getValue();
			DatasetDefinition datasetDefinition = mapDatasetDefinition.get(eachEntry.getKey());
			Map<String, List<Map<String, String>>> datas = datasetDefinition.getDatas();

			for (Map.Entry<String, List<Map<String, String>>> eachListEntry : datas.entrySet()) {
				try (Connection conn = dataSource1.getConnection()) {
					DatabaseUtils.cleanAllUsePreparedStatement(conn, eachListEntry.getKey());
				}
			}

		}
	}

	private static void initTableData(Map<String, DataSource> dataSourceMaps, Map<String, String> sqls,
			Map<String, DatasetDefinition> mapDatasetDefinition) throws SQLException, ParseException {
		for (Map.Entry<String, DataSource> eachDataSourceEntry : dataSourceMaps.entrySet()) {
			DataSource dataSource1 = eachDataSourceEntry.getValue();
			DatasetDefinition datasetDefinition = mapDatasetDefinition.get(eachDataSourceEntry.getKey());
			Map<String, Map<String, String>> configs = datasetDefinition.getConfigs();
			Map<String, List<Map<String, String>>> datas = datasetDefinition.getDatas();

			for (Map.Entry<String, List<Map<String, String>>> eachListEntry : datas.entrySet()) {
				try (Connection conn = dataSource1.getConnection()) {
					DatabaseUtils.insertUsePreparedStatement(conn, sqls.get(eachListEntry.getKey()),
							datas.get(eachListEntry.getKey()), configs.get(eachListEntry.getKey()));
				}
			}
		}
	}

}
