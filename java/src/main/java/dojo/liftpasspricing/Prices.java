package dojo.liftpasspricing;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

public class Prices {

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");

        port(4567);

        put("/prices", (req, res) -> {
            int liftPassCost = Integer.parseInt(req.queryParams("cost"));
            String liftPassType = req.queryParams("type");

            try (PreparedStatement stmt = connection.prepareStatement( //
                    "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                            "ON DUPLICATE KEY UPDATE cost = ?")) {
                stmt.setString(1, liftPassType);
                stmt.setInt(2, liftPassCost);
                stmt.setInt(3, liftPassCost);
                stmt.execute();
            }

            return "";
        });

        get("/prices", (req, res) -> {
            String age1 = req.queryParams("age");
            String type = req.queryParams("type");
            String date = req.queryParams("date");
            Function<String, ResultSet> getPrice = getPriceFunction().apply(connection);
            Supplier<ResultSet> getHolidays = getHolidaysFunction().apply(connection);

            try {
                return Model.getPrice(age1, type, date, getPrice, getHolidays);
            } finally {
                // TODO: make sure the db and resultset is closed properly
                // result.close();

                //  costStmt.close();
            }
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static Function<Connection, Supplier<ResultSet>> getHolidaysFunction() {
        return (connection) -> () -> {
            try {
                PreparedStatement holidayStmt = connection.prepareStatement( //
                        "SELECT * FROM holidays");

                return holidayStmt.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // FIXME return null
            return null;
        };
    }

    private static Function<Connection, Function<String, ResultSet>> getPriceFunction() {
        return (connection) -> (type) -> {
            try {
                PreparedStatement costStmt = connection.prepareStatement( //
                        "SELECT cost FROM base_price " + //
                                "WHERE type = ?");

                costStmt.setString(1, type);
                ResultSet result = costStmt.executeQuery();
                result.next();
                return result;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //TODO: Fix this
            return null;
        };
    }

}
