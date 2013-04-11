package seed;

import com.foxweave.connector.rdb.util.JDBCUtil;
import com.foxweave.connector.rdb.util.RDBConnector;
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
            Statement statement = connection.createStatement();
            try {
                StringBuilder batchString = new StringBuilder();
                for (int i = 0; i < 10000; i++) {
                    batchString.append("INSERT INTO test_data.guest.testdata_tbl (id, col1, col2, col3, col4, col5, col6) VALUES (" + i + ", 'col1-1-" + i + "', 'col1-2-" + i
                            + "', 'col1-3-" + i + "', 'col1-4-" + i + "', 'col1-5-" + i + "', 'col1-6-" + i + "');");
                    if (i % 100 == 0) {
                        System.out.println(i);
                        statement.execute(batchString.toString());
                        batchString.setLength(0);
                    }
                }
            } finally {
                JDBCUtil.safeClose(statement);
            }
        } finally {
            JDBCUtil.safeClose(connection);
        }
    }

    private static final String configString = "{\n" +
            "  \"authAccount\":\n" +
            "    {\n" +
            "      \"accountName\": \"localhost\",\n" +
            "      \"host\": \"sqlserver1.c4gxydnqwe4l.us-east-1.rds.amazonaws.com\",\n" +
            "      \"port\": \"1433\",\n" +
            "      \"username\": \"sqlserver1\",\n" +
            "      \"password\": \"sqlserver1\",\n" +
            "      \"driver\": \"net.sourceforge.jtds.jdbc.Driver\"\n" +
            "    },\n" +
            "  \"rdb_url\":\"jdbc:jtds:sqlserver://sqlserver1.c4gxydnqwe4l.us-east-1.rds.amazonaws.com:1433/test_data\"\n" +
            "}";


    // jdbc:jtds:sqlserver://" + component.accountSelected.host + ":" + component.accountSelected.port + "/" + db + ";domain="+ component.accountSelected.domain
}
