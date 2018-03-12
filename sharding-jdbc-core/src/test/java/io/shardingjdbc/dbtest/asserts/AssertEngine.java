package io.shardingjdbc.dbtest.asserts;


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
import io.shardingjdbc.dbtest.config.bean.parseContext.ParseContexDefinition;
import io.shardingjdbc.dbtest.core.common.util.SQLPlaceholderUtil;
import io.shardingjdbc.dbtest.core.parsing.parser.jaxb.helper.ParserAssertHelper;
import io.shardingjdbc.dbtest.core.parsing.parser.jaxb.helper.ParserJAXBHelper;
import io.shardingjdbc.dbtest.data.AnalyzeDataset;
import io.shardingjdbc.dbtest.data.DatasetDatabase;
import io.shardingjdbc.dbtest.data.DatasetDefinition;
import io.shardingjdbc.dbtest.dataSource.DataSourceUtil;
import io.shardingjdbc.dbtest.exception.DbTestException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * 用例执行引擎
 */
public class AssertEngine {

    public static void run() throws IOException, SAXException, SQLException, NoSuchFieldException, IllegalAccessException, XPathExpressionException, ParserConfigurationException, ParseException, JAXBException {
        String assertPath = ConfigRuntime.getAssertPath();
        assertPath = PathUtils.getPath(assertPath);

        //搜索所有用例
        List<String> paths = FileUtils.getAllFilePaths(new File(assertPath), "assert-", "xml");
        for (String path : paths) {
            runAssert(path);
        }
    }

    /**
     * 运行用例
     * @param path
     * @throws IOException
     * @throws SAXException
     * @throws SQLException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     */
    public static boolean runAssert(String path) throws IOException, SAXException, SQLException, NoSuchFieldException, IllegalAccessException, XPathExpressionException, ParserConfigurationException, ParseException, JAXBException {

        //解析用例
        AssertsDefinition assertsDefinition = AnalyzeConfig.analyze(path);
        String rootPath = path.substring(0, path.lastIndexOf(File.separator) + 1);
        assertsDefinition.setPath(rootPath);

        //获得主DataSource
        DataSource dataSource = DataSourceUtil.getDataSource(PathUtils.getPath(assertsDefinition.getShardingRuleConfig(), rootPath));

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
            //检查sql解析,是否正确
            if(anAssert.getParseContex() != null ){
                ParseContexDefinition expected = anAssert.getParseContex();

                SQLStatement actual = new SQLParsingEngine(shardingContext.getDatabaseType(), SQLPlaceholderUtil.replacePreparedStatement(rootsql), shardingRule).parse();

                ParserAssertHelper.assertTables(expected.getTables(), actual.getTables());
                ParserAssertHelper.assertConditions(expected.getConditions(), actual.getConditions(), false);
                ParserAssertHelper.assertSqlTokens(expected.getSqlTokens(), actual.getSqlTokens(), false);
                if (actual instanceof SelectStatement) {
                    SelectStatement selectStatement = (SelectStatement) actual;
                    SelectStatement expectedSqlStatement = ParserJAXBHelper.getSelectStatement(expected);
                    ParserAssertHelper.assertOrderBy(expectedSqlStatement.getOrderByItems(), selectStatement.getOrderByItems());
                    ParserAssertHelper.assertGroupBy(expectedSqlStatement.getGroupByItems(), selectStatement.getGroupByItems());
                    ParserAssertHelper.assertAggregationSelectItem(expectedSqlStatement.getAggregationSelectItems(), selectStatement.getAggregationSelectItems());
                    ParserAssertHelper.assertLimit(expected.getLimit(), selectStatement.getLimit(), false);
                }
            }

            String initDataFile = PathUtils.getPath(anAssert.getInitDataFile(), rootPath);
            DatasetDefinition datasetDefinition = null;
            Map<String,String> sqls = null;
            // 根据数据库 配置来查找对应的 init文件
            for (String db : dbs) {
                String tempPath = initDataFile + "/"+db+".xml";
                File file = new File(tempPath);
                if(file.exists()){
                    datasetDefinition = AnalyzeDataset.analyze(file);
                    sqls = new HashMap<>();
                    Map<String,List<Map<String,String>>>  datas = datasetDefinition.getDatas();
                    for (Map.Entry<String, List<Map<String,String>>> stringStringEntry : datas.entrySet()) {
                        String sql = DatabaseUtils.analyzeSql(stringStringEntry.getKey(),stringStringEntry.getValue().get(0));
                        sqls.put(stringStringEntry.getKey(),sql);
                    }
                }
            }

            if(datasetDefinition == null){
                throw new DbTestException(path+"  用例无法解析");
            }

            if(sqls == null){
                throw new DbTestException(path+"  用例无法初始化数据");
            }

            Map<String,List<Map<String,String>>> datas = datasetDefinition.getDatas();
            Map<String,Map<String,String>> configs = datasetDefinition.getConfigs();

            //执行用例
            if(DatabaseUtils.isSelect(rootsql)){
                initTableData(dataSourceMaps, sqls, datas, configs);
                try {
                    try (Connection con = dataSource.getConnection();) {
                        DatasetDatabase ddPreparedStatement = DatabaseUtils.selectUsePreparedStatement(con, rootsql, anAssert.getParameters());

                        String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
                        DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));

                        DatabaseUtils.assertDatas(checkDataset, ddPreparedStatement);
                    }

                    try (Connection con = dataSource.getConnection();) {
                        DatasetDatabase ddStatement = DatabaseUtils.selectUseStatement(con, rootsql, anAssert.getParameters());
                        String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
                        DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));

                        DatabaseUtils.assertDatas(checkDataset, ddStatement);
                    }
                }finally {
                    clearTableData(dataSourceMaps, datas);
                }


            }else{
                //updateUseStatementToExecuteUpdate
                initTableData(dataSourceMaps, sqls, datas, configs);
                try( Connection con = dataSource.getConnection();){
                    int num = DatabaseUtils.updateUseStatementToExecuteUpdate(con,rootsql,anAssert.getParameters());
                    String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
                    DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));
                    List<Map<String,String>> dataUpdate =  checkDataset.getDatas().get("int");
                    if(dataUpdate != null){
                        Assert.assertEquals(Long.valueOf(dataUpdate.get(0).get("value")).longValue(),num);
                    }
                }
                clearTableData(dataSourceMaps, datas);

                //updateUseStatementToExecute
                initTableData(dataSourceMaps, sqls, datas, configs);
                try( Connection con = dataSource.getConnection();){
                    boolean bool = DatabaseUtils.updateUseStatementToExecute(con,rootsql,anAssert.getParameters());
                    Assert.assertTrue(bool);


                }
                clearTableData(dataSourceMaps, datas);

                //updateUsePreparedStatementToExecuteUpdate
                initTableData(dataSourceMaps, sqls, datas, configs);
                try( Connection con = dataSource.getConnection();){
                    int num = DatabaseUtils.updateUsePreparedStatementToExecuteUpdate(con,rootsql,anAssert.getParameters());
                    String expectedDataFile = PathUtils.getPath(anAssert.getExpectedDataFile(), rootPath);
                    DatasetDefinition checkDataset = AnalyzeDataset.analyze(new File(expectedDataFile));
                    List<Map<String,String>> dataUpdate =  checkDataset.getDatas().get("int");
                    if(dataUpdate != null){
                        Assert.assertEquals(Long.valueOf(dataUpdate.get(0).get("value")).longValue(),num);
                    }
                }
                clearTableData(dataSourceMaps, datas);

                //updateUsePreparedStatementToExecute
                initTableData(dataSourceMaps, sqls, datas, configs);
                try( Connection con = dataSource.getConnection();){
                    boolean bool = DatabaseUtils.updateUsePreparedStatementToExecute(con,rootsql,anAssert.getParameters());
                    Assert.assertTrue(bool);
                }
                clearTableData(dataSourceMaps, datas);

            }

        }
        return true;
    }

    private static void clearTableData(Map<String, DataSource> dataSourceMaps, Map<String, List<Map<String, String>>> datas) throws SQLException {
        //清理数据
        for (Map.Entry<String, DataSource> stringDataSourceEntry : dataSourceMaps.entrySet()) {

            DataSource dataSource1 = stringDataSourceEntry.getValue();
            for (Map.Entry<String, List<Map<String, String>>> stringListEntry : datas.entrySet()) {
                try (Connection conn = dataSource1.getConnection()){
                    DatabaseUtils.cleanAllUsePreparedStatement(conn,stringListEntry.getKey());
                }
            }

        }
    }

    private static void initTableData(Map<String, DataSource> dataSourceMaps, Map<String, String> sqls, Map<String, List<Map<String, String>>> datas, Map<String, Map<String, String>> configs) throws SQLException, ParseException {
        //初始化数据
        for (Map.Entry<String, DataSource> stringDataSourceEntry : dataSourceMaps.entrySet()) {
            DataSource dataSource1 = stringDataSourceEntry.getValue();
            for (Map.Entry<String, List<Map<String, String>>> stringListEntry : datas.entrySet()) {
                try (Connection conn = dataSource1.getConnection()){
                    DatabaseUtils.insertUsePreparedStatement(conn,sqls.get(stringListEntry.getKey()),datas.get(stringListEntry.getKey()),configs.get(stringListEntry.getKey()));
                }
            }
        }
    }

}
