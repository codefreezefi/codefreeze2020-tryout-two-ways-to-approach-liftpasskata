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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Prices {

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");

        port(4567);

        put("/prices", (req, res) -> {
            return putPrices(connection, Integer.parseInt(req.queryParams("cost")), req.queryParams("type"));
        });

        get("/prices", (req, res) -> {
            return getPrices(connection, req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null, req.queryParams("type"), req.queryParams("date"));
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static String getPrices(Connection connection, Integer age, String type, String date) throws SQLException, ParseException {

        try (PreparedStatement costStmt = connection.prepareStatement( //
                "SELECT cost FROM base_price " + //
                        "WHERE type = ?")) {
            costStmt.setString(1, type);
            try (ResultSet result = costStmt.executeQuery()) {
                result.next();

                return calculateCost(age, type, result.getInt("cost"), getDate(date), isHoliday(connection, getDate(date)));
            }
        }
    }

    private static String calculateCost(Integer age, String type, int baseCost, Date date, boolean isHoliday) {
        int reduction;

        if (age != null && age < 6) {
            return "{ \"cost\": 0}";
        } else {
            reduction = 0;

            if (isDay(type)) {

                if (date != null) {
                    if (!isHoliday) {
                        if (isMonday(date)) {
                            reduction = 35;
                        }
                    }
                }

                // TODO apply reduction for others
                if (age != null && age < 15) {
                    return "{ \"cost\": " + (int) Math.ceil(baseCost * .7) + "}";
                } else {
                    if (age == null) {
                        double cost = baseCost * (1 - reduction / 100.0);
                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                    } else {
                        if (age > 64) {
                            double cost = baseCost * .75 * (1 - reduction / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        } else {
                            double cost = baseCost * (1 - reduction / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        }
                    }
                }
            } else {
                if (age != null && age >= 6) {
                    if (age > 64) {
                        return "{ \"cost\": " + (int) Math.ceil(baseCost * .4) + "}";
                    } else {
                        return "{ \"cost\": " + baseCost + "}";
                    }
                } else {
                    return "{ \"cost\": 0}";
                }
            }
        }
    }

    private static boolean isMonday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK) == 2;
    }

    private static boolean isDay(String type) {
        return !type.equals("night");
    }

    private static boolean isHoliday(Connection connection, Date date) throws SQLException {
        boolean isHoliday = false;
        try (PreparedStatement holidayStmt = connection.prepareStatement( //
                "SELECT * FROM holidays")) {
            try (ResultSet holidays = holidayStmt.executeQuery()) {

                while (holidays.next()) {
                    Date holiday = holidays.getDate("holiday");
                    if (date != null) {
                        if (date.getYear() == holiday.getYear() && //
                                date.getMonth() == holiday.getMonth() && //
                                date.getDate() == holiday.getDate()) {
                            isHoliday = true;
                        }
                    }
                }

            }
        }
        return isHoliday;
    }

    private static Date getDate(String date) throws ParseException {
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        if (date != null) {
            d = isoFormat.parse(date);
        }
        return d;
    }

    private static String putPrices(Connection connection, int liftPassCost, String liftPassType) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement( //
                "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                        "ON DUPLICATE KEY UPDATE cost = ?")) {
            stmt.setString(1, liftPassType);
            stmt.setInt(2, liftPassCost);
            stmt.setInt(3, liftPassCost);
            stmt.execute();
        }

        return "";
    }

}
