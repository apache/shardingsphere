package mainBYYASH.java;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        try {
            Class.forName("org.apache.shardingsphere.driver.ShardingSphereDriver");
            String file_path = "C:\\Users\\yasht\\Apache_Sharding_Sphere\\shardingsphere\\jdbc\\src\\test\\java\\mainBYYASH\\resources\\config.yaml";
            File yamlFile = new File(file_path);
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
            Connection con = dataSource.getConnection();
            Statement st = con.createStatement();


//            ResultSet rs = st.executeQuery("select *,lead(name,1) over (order by id) as 'Name_Lag' from t");


//              ResultSet rs = st.executeQuery("SELECT * from t where name not like 'ok' and name not like 'ko' and name not like 'Tiwari';");

//                ResultSet rs = st.executeQuery("SELECT *,NTILE(2) over (order by id) as 'TIL' from t;");
//
            st.execute("SET @schema = '{"
                    + "\"$schema\": \"https://json-schema.org/draft/2020-12/schema\","
                    + "\"type\": \"object\","
                    + "\"properties\": {"
                    + "\"name\": {\"type\": \"string\"},"
                    + "\"age\": {\"type\": \"integer\"},"
                    + "\"email\": {\"type\": \"string\"}"
                    + "},"
                    + "\"required\": [\"name\", \"age\"]"
                    + "}';");
            st.execute( "SET @document = '{\"name\": \"Alice\",\"age\": \"ani\",\"email\": \"alice@example.com\"}';" );
//            ResultSet rs = st.executeQuery("SELECT JSON_SCHEMA_VALIDATION_REPORT('{"
//                    + "\"$schema\": \"https://json-schema.org/draft/2020-12/schema\","
//                    + "\"type\": \"object\","
//                    + "\"properties\": {"
//                    + "\"name\": {\"type\": \"string\"},"
//                    + "\"age\": {\"type\": \"integer\"},"
//                    + "\"email\": {\"type\": \"string\"}"
//                    + "},"
//                    + "\"required\": [\"name\", \"age\"]"
//                    + "}', @document);");

            ResultSet rs = st.executeQuery("SELECT JSON_SCHEMA_VALIDATION_REPORT( @schema, @document);");
//            ResultSet rs = st.executeQuery(
//                    "SELECT JSON_SCHEMA_VALIDATION_REPORT("
//                            + "'{\"$schema\": \"https://json-schema.org/draft/2020-12/schema\","
//                            + "\"type\": \"object\","
//                            + "\"properties\": {\"name\": {\"type\": \"string\"},"
//                            + "\"age\": {\"type\": \"integer\"},"
//                            + "\"email\": {\"type\": \"string\"}},"
//                            + "\"required\": [\"name\", \"age\"]}',"
//                            + "'{\"name\": \"Alice\", \"age\": 25.2, \"email\": \"alice@example.com\"}');"
//            );


            while(rs.next())
            {
//                System.out.println(rs.getLong(1) + " " + rs.getString(2) + " " + rs.getString(3));
//                System.out.println(rs.getInt(1));
//                System.out.println(rs.getLong(1) + " " + rs.getString(2));
                System.out.println(rs.getString(1));
            }

        }
        catch(ClassNotFoundException | SQLException e )
        {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}