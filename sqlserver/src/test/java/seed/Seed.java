package seed;

import com.foxweave.connector.rdb.util.JDBCUtil;
import com.foxweave.connector.rdb.util.RDBConnector;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.Random;

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
                Random random = new Random();
                int i = 0;

                for (; i < 10000; i++) {
                    int randCustomerId = random.nextInt(4);
                    int randProductId = random.nextInt(4);
                    int randQuantity = random.nextInt(10) + 1;

                    batchString.append("INSERT INTO sales.guest.sale (id, customerid, productid, quantity, date) VALUES  ("
                            + i + ", "
                            + randCustomerId + ", "
                            + randProductId + ", "
                            + randQuantity + ", "
                            + "'20120618 10:34:09 AM'" + ");");

                    if (i % 100 == 0 && batchString.length() > 0) {
                        System.out.println(i);
                        statement.execute(batchString.toString());
                        batchString.setLength(0);
                    }
                }

                if (batchString.length() > 0) {
                    System.out.println(i);
                    statement.execute(batchString.toString());
                    batchString.setLength(0);
                }
            } finally {
                JDBCUtil.safeClose(statement);
            }
        } finally {
            JDBCUtil.safeClose(connection);
        }


        // select #s.id, #s.customerid, #s.productid, #s.quantity, #c.fname, #c.lname, #c.cntry, #p.name, #p.price, #s.date from customer c, product p, sale s where s.id > ##s.id and c.id = s.customerid and p.id = s.productid;
    }

    private static final String configString = "{\n" +
            "  \"authAccount\":\n" +
            "    {\n" +
            "      \"accountName\": \"localhost\",\n" +
            "      \"host\": \"aws-sqlserver.c4gxydnqwe4l.us-east-1.rds.amazonaws.com\",\n" +
            "      \"port\": \"1433\",\n" +
            "      \"username\": \"sqlserver1\",\n" +
            "      \"password\": \"sqlserver1\",\n" +
            "      \"driver\": \"net.sourceforge.jtds.jdbc.Driver\"\n" +
            "    },\n" +
            "  \"rdb_url\":\"jdbc:jtds:sqlserver://aws-sqlserver.c4gxydnqwe4l.us-east-1.rds.amazonaws.com:1433/test_data\"\n" +
            "}";


    // jdbc:jtds:sqlserver://" + component.accountSelected.host + ":" + component.accountSelected.port + "/" + db + ";domain="+ component.accountSelected.domain



}
