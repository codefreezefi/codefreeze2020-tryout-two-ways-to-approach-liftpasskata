package dojo.liftpasspricing;

import java.sql.ResultSet;
import java.util.function.Function;
import java.util.function.Supplier;

public class Repository {
    private final Function<String, ResultSet> getPrice;
    private final Supplier<ResultSet> getHolidays;

    public Repository(Function<String, ResultSet> getPrice, Supplier<ResultSet> getHolidays) {
        this.getPrice = getPrice;
        this.getHolidays = getHolidays;
    }

    public Function<String, ResultSet> getGetPrice() {
        return getPrice;
    }

    public Supplier<ResultSet> getGetHolidays() {
        return getHolidays;
    }
}
