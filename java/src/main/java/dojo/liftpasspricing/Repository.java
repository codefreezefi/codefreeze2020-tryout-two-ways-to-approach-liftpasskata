package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class Repository {

    private Connection connection;

    public Repository(Connection connection) {
        this.connection = connection;
    }

    int getBaseCost(String type) throws SQLException {
        int cost = 0;
        try (PreparedStatement costStmt = connection.prepareStatement( //
                "SELECT cost FROM base_price " + //
                        "WHERE type = ?")) {
            costStmt.setString(1, type);
            try (ResultSet result = costStmt.executeQuery()) {
                result.next();

                cost = result.getInt("cost");
            }
        }
        return cost;
    }

    String putPrices(int liftPassCost, String liftPassType) throws SQLException {
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

    boolean isHoliday(Date date, LocalDate localDate) throws SQLException {
        boolean isHoliday = false;
        try (PreparedStatement holidayStmt = this.connection.prepareStatement( //
                "SELECT * FROM holidays")) {
            try (ResultSet holidays = holidayStmt.executeQuery()) {

                while (holidays.next()) {
                    java.sql.Date holiday = holidays.getDate("holiday");
                    if (localDate != null) {

                        if (localDate.equals(holiday.toLocalDate())) {
                            isHoliday = true;
                        }
                    }
                }

            }
        }
        return isHoliday;
    }
}