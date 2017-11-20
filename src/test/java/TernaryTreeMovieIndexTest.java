import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class TernaryTreeMovieIndexTest {

    /**
     * Ensures indexing and querying works as expected.
     */
    @Test
    public void testIndex() throws ExecutionException, InterruptedException {
        String movies = getClass().getResource("movies.txt").getFile();
        MovieReader reader = new MovieFileReader(movies);
        MovieIndex index = new TernaryTreeMovieIndex();

        // Index the reader and wait for it to complete.
        Future<?> future = index.index(reader);
        future.get();

        List<Movie> results = index.query("foo", 5);
        assertEquals(0, results.size());

        results = index.query("star", 5);
        assertEquals(2, results.size());
        assertEquals("Audi Star Talk", results.get(0).getTitle());
        assertEquals("Starship Troopers 3", results.get(1).getTitle());

        results = index.query("the", 3);
        assertEquals(3, results.size());
        assertEquals("Guardians of the Galaxy", results.get(0).getTitle());
        assertEquals("Harry Potter and the Deathly Hollows Part 1", results.get(1).getTitle());
        assertEquals("Harry Potter and the Deathly Hollows Part 2", results.get(2).getTitle());

        results = index.query("2", 5);
        assertEquals(4, results.size());
        assertEquals("Cars 2", results.get(0).getTitle());
        assertEquals("Harry Potter and the Deathly Hollows Part 2", results.get(1).getTitle());
        assertEquals("Iron Man 2", results.get(2).getTitle());
        assertEquals("Kung Fu Panda 2", results.get(3).getTitle());

        results = index.query("de", 5);
        assertEquals(3, results.size());
        assertEquals("Despicable Me", results.get(0).getTitle());
        assertEquals("Harry Potter and the Deathly Hollows Part 1", results.get(1).getTitle());
        assertEquals("Harry Potter and the Deathly Hollows Part 2", results.get(2).getTitle());

        results = index.query("Part 1", 5);
        assertEquals(2, results.size());
        assertEquals("Harry Potter and the Deathly Hollows Part 1", results.get(0).getTitle());
        assertEquals("The Twilight Saga: Breaking Dawn Part 1", results.get(1).getTitle());
    }

}
