import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class MovieFileReaderTest {

    /**
     * Opening a {@code MovieFileReader} for a file that doesn't exist should throw a {@code FileNotFoundException}.
     */
    @Test(expected = FileNotFoundException.class)
    public void testFileNotFound() throws IOException {
        MovieReader reader = new MovieFileReader("foo.txt");
        reader.open();
    }

    /**
     * Calling {@code next} on a {@code MovieFileReader} throws an {@code IllegalStateException} if it has not been
     * opened.
     */
    @Test(expected = IllegalStateException.class)
    public void testNextNotOpen() {
        MovieReader reader = new MovieFileReader("valid.txt");
        reader.next();
    }

    /**
     * Calling {@code hasNext} on a {@code MovieFileReader} throws an {@code IllegalStateException} if it has not been
     * opened.
     */
    @Test(expected = IllegalStateException.class)
    public void testHasNextNotOpen() {
        MovieReader reader = new MovieFileReader("valid.txt");
        reader.hasNext();
    }


    /**
     * A {@code NoSuchElementException} should be thrown for each incorrectly formatted line.
     */
    @Test
    public void testInvalidLines() throws IOException {
        String invalid = getClass().getResource("invalid.txt").getFile();
        MovieReader reader = new MovieFileReader(invalid);
        reader.open();
        List<Movie> movies = new ArrayList<>();
        int count = 0;
        try (reader) {
            while (reader.hasNext()) {
                try {
                    Movie m = reader.next();
                    movies.add(m);
                } catch (NoSuchElementException e) {
                    count++;
                }
            }
        }
        assertEquals(2, count);
        assertEquals(3, movies.size());
        assertEquals(1999, movies.get(0).getReleaseYear());
        assertEquals("US", movies.get(0).getCountryCode());
        assertEquals("The Matrix", movies.get(0).getTitle());
        assertEquals(2003, movies.get(1).getReleaseYear());
        assertEquals("US", movies.get(1).getCountryCode());
        assertEquals("The Matrix Reloaded", movies.get(1).getTitle());
        assertEquals(2003, movies.get(2).getReleaseYear());
        assertEquals("US", movies.get(2).getCountryCode());
        assertEquals("The Matrix Revolutions", movies.get(2).getTitle());
    }

    /**
     * {@code MovieFileReader} returns the correct {@code Movie} objects when reading a correctly formatted file.
     */
    @Test
    public void testReader() throws IOException {
        String valid = getClass().getResource("valid.txt").getFile();
        MovieReader reader = new MovieFileReader(valid);
        reader.open();
        List<Movie> movies = new ArrayList<>();
        try (reader) {
            while (reader.hasNext()) {
                Movie m = reader.next();
                movies.add(m);
            }
        }
        assertEquals(3, movies.size());
        assertEquals(1999, movies.get(0).getReleaseYear());
        assertEquals("US", movies.get(0).getCountryCode());
        assertEquals("The Matrix", movies.get(0).getTitle());
        assertEquals(2003, movies.get(1).getReleaseYear());
        assertEquals("US", movies.get(1).getCountryCode());
        assertEquals("The Matrix Reloaded", movies.get(1).getTitle());
        assertEquals(2003, movies.get(2).getReleaseYear());
        assertEquals("US", movies.get(2).getCountryCode());
        assertEquals("The Matrix Revolutions", movies.get(2).getTitle());
    }
}
