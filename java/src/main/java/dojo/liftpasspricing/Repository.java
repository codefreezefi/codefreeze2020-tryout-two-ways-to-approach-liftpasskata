package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

public class Repository {
    private final Function<String, ResultSet> getPrice;
    private final Supplier<ResultSet> getHolidays;

    public Repository(Function<String, ResultSet> getPrice, Supplier<ResultSet> getHolidays) {
        this.getPrice = getPrice;
        this.getHolidays = getHolidays;
    }

    static Function<Connection, Supplier<ResultSet>> getHolidaysFunction() {
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

    static Function<Connection, Function<String, ResultSet>> getPriceFunction() {
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

    public Function<String, ResultSet> getGetPrice() {
        return getPrice;
    }

    public Supplier<ResultSet> getGetHolidays() {
        return getHolidays;
    }
}
