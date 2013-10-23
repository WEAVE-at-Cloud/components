package seed;

import com.foxweave.connector.rdb.util.RDBConnector;
import com.foxweave.jdbc.JDBCUtil;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.Statement;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Seed {

    public static void main(String[] args) throws Exception {
        JSONObject config = new JSONObject(configString);
        RDBConnector dbConnector = new RDBConnector(config);

        dbConnector.initialize();

        Connection connection = dbConnector.getConnection();
        try {
            for (int i = 10001; i < 10002; i++) {
                Statement statement = connection.createStatement();
                try {
                    String sql = "INSERT INTO testdata_tbl values (" + i + ", \"col1-1-" + i + "\", \"col1-2-" + i
                            + "\", \"col1-3-" + i + "\", \"col1-4-" + i + "\", \"col1-5-" + i + "\", \"col1-6-" + i + "\", " + (i % 10) + ");";
//                    String sql = "INSERT INTO foreign_tbl values (" + i + ", \"val-" + i + "\");";
//                    System.out.println(sql);
                    if (i % 100 == 0) {
                        System.out.println(i);
                    }
                    statement.execute(sql);
                } finally {
                    JDBCUtil.safeClose(statement);
                }
            }
        } finally {
            JDBCUtil.safeClose(connection);
        }
    }

    private static final String configString = "{\n" +
            "  \"authAccount\":\n" +
            "    {\n" +
            "      \"accountName\": \"localhost\",\n" +
            "      \"host\": \"localhost\",\n" +
            "      \"port\": \"3306\",\n" +
            "      \"username\": \"root\",\n" +
            "      \"password\": \"\",\n" +
            "      \"driver\": \"com.mysql.jdbc.Driver\"\n" +
            "    },\n" +
            "  \"rdb_url\":\"jdbc:mysql://localhost:3306/test_data\"\n" +
            "}";
}
