import java.util.List;
import java.util.concurrent.Future;

/**
 * {@code MovieIndex} is used to index {@link Movie} objects by title to enable type-ahead searches.
 */
public interface MovieIndex {

    /**
     * Add the {@link Movie} objects from the given {@link MovieReader} to the search index. This is done
     * asynchronously, meaning a query run immediately after this is called might not reflect the items indexed here.
     *
     * @param reader Movies to index
     * @return {@link Future} to wait on the indexing
     */
    Future<?> index(MovieReader reader);

    /**
     * Returns a list of {link Movie} objects whose titles prefix match any word in the title, up to {@code limit}
     * results, and sorted in alphabetical order of the title.
     *
     * @param query search string
     * @param limit max number of results
     * @return list of {@code Movie} objects in alphabetical order
     */
    List<Movie> query(String query, int limit);

}
