package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static spark.Spark.*;

public class Prices {

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");
        Repository repository = new Repository(connection);

        port(4567);

        put("/prices", (req, res) -> {
            return repository.putPrices(Integer.parseInt(req.queryParams("cost")), req.queryParams("type"));
        });

        get("/prices", (req, res) -> {
            int cost = getPrices(req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null, req.queryParams("type"), req.queryParams("date"), repository);
            return "{ \"cost\": " + cost + "}";
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static int getPrices(Integer age, String type, String dateString, Repository repository) throws SQLException {
        int baseCost = repository.getBaseCost(type);
        LocalDate localDate = getLocalDate(dateString);
        return calculateCost(age, type, baseCost, repository.isHoliday(localDate), localDate);
    }

    private static int calculateCost(Integer age, String type, int baseCost, boolean isHoliday, LocalDate localDate) {
        int cost = 0;

        if (isDay(type)) {
            cost = calculateCostForDayTicket(age, baseCost, isHoliday, localDate);
        } else {
            cost = calculateCostForNightTicket(age, baseCost);
        }

        return cost;
    }

    private static int calculateCostForNightTicket(Integer age, int baseCost) {
        if (age == null) {
            return 0;
        }

        if (age < 6) {
            return 0;
        }

        if (age > 64) {
            return (int) Math.ceil(baseCost * .4);
        }

        return baseCost;
    }

    private static int calculateCostForDayTicket(Integer age, int baseCost, boolean isHoliday, LocalDate localDate) {

        if (age != null && age < 6) {
            return 0;
        }

        // TODO apply reduction for others
        if (age != null && age < 15) {
            return (int) Math.ceil(baseCost * .7);
        }

        int reduction = calculateReduction(isHoliday, localDate);

        if (age != null && age > 64) {
            return (int) Math.ceil(baseCost * .75 * (1 - reduction / 100.0));
        }

        return (int) Math.ceil(baseCost * (1 - reduction / 100.0));
    }

    private static int calculateReduction(boolean isHoliday, LocalDate localDate) {
        int reduction = 0;
        if (localDate != null && !isHoliday && localDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            reduction = 35;
        }
        return reduction;
    }

    private static boolean isDay(String type) {
        return !type.equals("night");
    }

    private static LocalDate getLocalDate(String date) {
        LocalDate d = null;
        if (date != null) {
            d = LocalDate.parse(date);
        }
        return d;
    }

}
