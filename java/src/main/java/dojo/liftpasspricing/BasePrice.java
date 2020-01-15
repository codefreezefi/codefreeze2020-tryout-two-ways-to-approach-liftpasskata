package dojo.liftpasspricing;

import java.sql.SQLException;

public interface BasePrice {
    int getInt(String cost) throws SQLException;
}
