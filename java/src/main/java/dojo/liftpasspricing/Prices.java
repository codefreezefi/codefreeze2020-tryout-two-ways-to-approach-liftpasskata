package dojo.liftpasspricing;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Prices {

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");
        Repository repository = new Repository(connection);

        port(4567);

        put("/prices", (req, res) -> {
            return repository.putPrices(Integer.parseInt(req.queryParams("cost")), req.queryParams("type"));
        });

        get("/prices", (req, res) -> {
            return getPrices(req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null, req.queryParams("type"), req.queryParams("date"), repository);
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static String getPrices(Integer age, String type, String date, Repository repository) throws SQLException, ParseException {

        int cost = repository.getBaseCost(type);
        return calculateCost(age, type, cost, getDate(date), repository.isHoliday(getDate(date)));
    }

    private static String calculateCost(Integer age, String type, int baseCost, Date date, boolean isHoliday) {

        if (age != null && age < 6) {
            return "{ \"cost\": 0}";
        }

        if (isDay(type)) {

            return calculateCostForDayTicket(age, baseCost, date, isHoliday);
        }

        if (age != null) {
            return calculateCostForNightTicket(age, baseCost);
        }

        return "{ \"cost\": 0}";
    }

    private static String calculateCostForNightTicket(Integer age, int baseCost) {
        if (age > 64) {
            return "{ \"cost\": " + (int) Math.ceil(baseCost * .4) + "}";
        }

        return "{ \"cost\": " + baseCost + "}";
    }

    private static String calculateCostForDayTicket(Integer age, int baseCost, Date date, boolean isHoliday) {
        int reduction = calculateReduction(date, isHoliday);

        if (age == null) {
            double cost = baseCost * (1 - reduction / 100.0);
            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
        }

        // TODO apply reduction for others
        if (age < 15) {
            return "{ \"cost\": " + (int) Math.ceil(baseCost * .7) + "}";
        }

        if (age > 64) {
            double cost = baseCost * .75 * (1 - reduction / 100.0);
            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
        }

        double cost = baseCost * (1 - reduction / 100.0);
        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
    }

    private static int calculateReduction(Date date, boolean isHoliday) {
        int reduction = 0;
        if (date != null) {
            if (!isHoliday) {
                if (isMonday(date)) {
                    reduction = 35;
                }
            }
        }
        return reduction;
    }

    private static boolean isMonday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK) == 2;
    }

    private static boolean isDay(String type) {
        return !type.equals("night");
    }

    private static Date getDate(String date) throws ParseException {
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        if (date != null) {
            d = isoFormat.parse(date);
        }
        return d;
    }

}
