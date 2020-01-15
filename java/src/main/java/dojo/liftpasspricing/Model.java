package dojo.liftpasspricing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;

public class Model {
    static String getPrice(String age1, String type, String date, Function<String, ResultSet> getPrice, Supplier<ResultSet> getHolidays) throws SQLException, ParseException {
        final Integer age = age1 != null ? Integer.valueOf(age1) : null;

        ResultSet result = getPrice.apply(type);


        int reduction;
        boolean isHoliday = false;

        if (age != null && age < 6) {
            return "{ \"cost\": 0}";
        } else {
            reduction = 0;

            if (!type.equals("night")) {
                DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                ResultSet holidays = getHolidays.get();
                try {

                    while (holidays.next()) {
                        Date holiday = holidays.getDate("holiday");
                        if (date != null) {
                            Date d = isoFormat.parse(date);
                            if (d.getYear() == holiday.getYear() && //
                                    d.getMonth() == holiday.getMonth() && //
                                    d.getDate() == holiday.getDate()) {
                                isHoliday = true;
                            }
                        }
                    }

                } finally {
                    // TODO: fix db sessions
                    // holidays.close();

                    // holidayStmt.close();
                }

                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(isoFormat.parse(date));
                    if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                        reduction = 35;
                    }
                }

                // TODO apply reduction for others
                if (age != null && age < 15) {
                    return "{ \"cost\": " + (int) Math.ceil(result.getInt("cost") * .7) + "}";
                } else {
                    if (age == null) {
                        double cost = result.getInt("cost") * (1 - reduction / 100.0);
                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                    } else {
                        if (age > 64) {
                            double cost = result.getInt("cost") * .75 * (1 - reduction / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        } else {
                            double cost = result.getInt("cost") * (1 - reduction / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        }
                    }
                }
            } else {
                if (age != null && age >= 6) {
                    if (age > 64) {
                        return "{ \"cost\": " + (int) Math.ceil(result.getInt("cost") * .4) + "}";
                    } else {
                        return "{ \"cost\": " + result.getInt("cost") + "}";
                    }
                } else {
                    return "{ \"cost\": 0}";
                }
            }
        }
    }
}
