import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code TernaryTreeMovieIndex} implements the {@link MovieIndex} interface by using a fixed-size thread pool to
 * asynchronously index Movies and storing them in a ternary search tree.
 */
public class TernaryTreeMovieIndex implements MovieIndex {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));

    @Override
    public void index(MovieReader reader) {
        executor.execute(() -> {
            try (reader) {
                reader.open();
                while (reader.hasNext()) {
                    Movie movie = reader.next();
                    System.out.println(movie);
                    // TODO
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<Movie> query(String query, int limit) {
        return null;
    }

}
