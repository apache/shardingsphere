import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;


import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

import java.sql.*;

public class Main {
    public static void main(String[] args) {

//        com.zaxxer.hikari.HikariDataSource --> its the class  structure for Hikari datasource..
        try {
            Class.forName("org.apache.shardingsphere.driver.ShardingSphereDriver");
            String file_path = "C:\\Users\\yasht\\Apache_Sharding_Sphere\\shardingsphere\\examples\\MyExample\\src\\main\\resources\\config.yaml";
            File yamlFile = new File(file_path);
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
            Connection con = dataSource.getConnection();
            Statement st = con.createStatement();

//            st.executeUpdate("insert into t(Name) values('Hunger');");
//            ResultSet rs = st.executeQuery("select * from t where t.Name = REPEAT('A',3);");
//
//            while( rs.next() )
//            {
//                System.out.println(rs.getLong(1) + "  " + rs.getString(2) );
//            }

            ResultSet rs = st.executeQuery("SELECT IS_IPV6('10.0.5.9'), IS_IPV6('::1');");
            while(rs.next())
            {
                System.out.println(rs.getInt(1)+" "+rs.getInt(2));
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

