package dojo.liftpasspricing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Model {
    static String getPrice(Query query, Repository repository) throws ParseException {
        final Integer age = query.getAge1() != null ? Integer.valueOf(query.getAge1()) : null;

        BasePrice basePrice = repository.getGetPrice().apply(query.getType());

        int reduction;
        boolean isHoliday = false;

        if (age != null && age < 6) {
            return "{ \"cost\": 0}";
        } else {
            reduction = 0;

            if (!query.getType().equals("night")) {
                DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                Holidays holidays = repository.getGetHolidays().get();
                try {

                    while (holidays.next()) {
                        Date holiday = holidays.getDate();
                        if (query.getDate() != null) {
                            Date d = isoFormat.parse(query.getDate());
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

                if (query.getDate() != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(isoFormat.parse(query.getDate()));
                    if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                        reduction = 35;
                    }
                }

                // TODO apply reduction for others
                if (age != null && age < 15) {
                    return "{ \"cost\": " + (int) Math.ceil(basePrice.get() * .7) + "}";
                } else {
                    if (age == null) {
                        double cost = basePrice.get() * (1 - reduction / 100.0);
                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                    } else {
                        if (age > 64) {
                            double cost = basePrice.get() * .75 * (1 - reduction / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        } else {
                            double cost = basePrice.get() * (1 - reduction / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        }
                    }
                }
            } else {
                if (age != null && age >= 6) {
                    if (age > 64) {
                        return "{ \"cost\": " + (int) Math.ceil(basePrice.get() * .4) + "}";
                    } else {
                        return "{ \"cost\": " + basePrice.get() + "}";
                    }
                } else {
                    return "{ \"cost\": 0}";
                }
            }
        }
    }
}
