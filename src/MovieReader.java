import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * {@code MovieReader} is an {@link Iterator} of {@link Movie} objects which may be backed by a resource, such as a
 * file.
 */
public interface MovieReader extends Iterator<Movie>, Closeable {

    /**
     * Prepares the {@code MovieReader} for use.
     */
    void open() throws IOException;

}
