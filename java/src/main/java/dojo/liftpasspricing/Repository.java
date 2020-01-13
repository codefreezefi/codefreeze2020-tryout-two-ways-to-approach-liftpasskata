package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Repository {
    static boolean isHoliday(Connection connection, Date date) throws SQLException {
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
}