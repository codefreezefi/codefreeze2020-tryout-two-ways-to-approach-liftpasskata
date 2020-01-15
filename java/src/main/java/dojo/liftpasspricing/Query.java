package dojo.liftpasspricing;

public class Query {
    private final String age1;
    private final String type;
    private final String date;

    public Query(String age1, String type, String date) {
        this.age1 = age1;
        this.type = type;
        this.date = date;
    }

    public String getAge1() {
        return age1;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }
}
