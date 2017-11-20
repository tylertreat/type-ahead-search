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

    String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return String.format("%d\t%s\t%s", releaseYear, countryCode, title);
    }

    @Override
    public int hashCode() {
        int result = 3;
        result *= 31 + releaseYear;
        result *= 31 + countryCode.hashCode();
        result *= 31 + title.hashCode();
        return result;
    }
}
