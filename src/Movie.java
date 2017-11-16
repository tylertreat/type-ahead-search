/**
 * {@code Movie} is a domain object representing a catalog title.
 */
public class Movie {

    private final int releaseYear;
    private final String countryCode;
    private final String title;

    Movie(String title, String countryCode, int releaseYear) {
        this.title = title;
        this.countryCode = countryCode;
        this.releaseYear = releaseYear;
    }

    @Override
    public String toString() {
        return String.format("%d\t%s\t%s", releaseYear, countryCode, title);
    }
}
