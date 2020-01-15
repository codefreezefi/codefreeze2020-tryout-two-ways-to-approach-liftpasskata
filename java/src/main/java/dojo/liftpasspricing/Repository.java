package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;

public class Repository {
    private final Function<String, ResultSet> getPrice;
    private final Supplier<ResultSet> getHolidays;

    public Repository(Connection connection) {
        this.getPrice = getPriceFunction().apply(connection);
        this.getHolidays = getHolidaysFunction().apply(connection);
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

    public Function<String, BasePrice> getGetPrice() {
        return (type) -> {
            ResultSet resultSet = getPrice.apply(type);
            return new BasePrice() {
                @Override
                public int get() {
                    try {
                        return resultSet.getInt("cost");
                    } catch (SQLException e) {
                        // TODO: double check if this is supposed to happen if there is no cost in the db
                        return 0;
                    }
                }
            };
        };
    }

    public Supplier<Holidays> getGetHolidays() {
        return () -> {
            ResultSet resultSet = getHolidays.get();
            return new Holidays() {
                @Override
                public Date getDate() {
                    try {
                        return resultSet.getDate("holiday");
                    } catch (SQLException e) {
                        // FIXME: returns null
                        return null;
                    }
                }

                @Override
                public boolean next() {
                    try {
                        return resultSet.next();
                    } catch (SQLException e) {
                        return false;
                    }
                }
            };
        };
    }
}
