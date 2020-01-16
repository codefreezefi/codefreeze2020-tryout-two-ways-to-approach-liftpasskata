package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Repository {
    private final Function<String, ResultSet> getPrice;
    private final Connection connection;

    public Repository(Connection connection) {
        this.getPrice = getPriceFunction().apply(connection);
        this.connection = connection;
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
            List<Date> dates = getDates();
            return new Holidays() {
                int i = 0;

                @Override
                public Date getDate() {
                    return dates.get(i);
                }

                @Override
                public boolean next() {
                    i = i + 1;
                    return i < dates.size() - 1;
                }
            };
        };
    }

    private List<Date> getDates() {
        PreparedStatement holidayStmt = null;
        ResultSet resultSet = null;
        try {
            List<Date> dates = new ArrayList();

            holidayStmt = connection.prepareStatement( //
                    "SELECT * FROM holidays");

            resultSet = holidayStmt.executeQuery();
            while (resultSet.next()) {
                dates.add(resultSet.getDate("holiday"));
            }
            return dates;
        } catch (SQLException e) {
            return new ArrayList<>();
        } finally {
            close(holidayStmt);
            close(resultSet);
        }
    }

    private void close(AutoCloseable resource) {
        if (null != resource) {
            try {
                resource.close();
            } catch (Exception e) {

            }
        }
    }
}
