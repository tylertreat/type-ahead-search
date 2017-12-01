import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@code MovieFileReader} is responsible for parsing text files containing movie data and converting them to
 * {@link Movie} objects. Text files are line-oriented and in the form [Year of Release]\t[Country-Code]\t[Movie-title].
 * It implements the {@link Iterator} interface such that it can stream {@code Movie} objects from the file.
 */
public class MovieFileReader implements MovieReader {

    private final String fileName;
    private BufferedReader reader;
    private boolean open;
    private String nextLine;

    /**
     * Creates a new {@code MovieFileReader} which will read from the file at the given path.
     *
     * @param file file path to read from
     */
    MovieFileReader(String file) {
        fileName = file;
    }

    /**
     * Opens the file to begin reading. This must be called before the {@code Iterator} can be used or an
     * {@link IllegalStateException} is thrown.
     *
     * @throws FileNotFoundException if the file does not exist
     */
    public synchronized void open() throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(fileName));
        open = true;
    }

    @Override
    public synchronized boolean hasNext() {
        if (!open) {
            throw new IllegalStateException("MovieFileReader not open");
        }
        // Read ahead in the file to ensure there is more.
        try {
            nextLine = reader.readLine();
            return nextLine != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public synchronized Movie next() {
        if (!open) {
            throw new IllegalStateException("MovieFileReader not open");
        }
        // Read the next line in the file if hasNext() wasn't called.
        if (nextLine == null) {
            try {
                nextLine = reader.readLine();
                if (nextLine == null) {
                    throw new NoSuchElementException();
                }
            } catch (IOException e) {
                throw new NoSuchElementException();
            }
        }

        // Convert tab-delimited string to Movie. We'll just raise a NoSuchElementException for now if the file is
        // improperly formatted.
        String[] components = nextLine.split("\t");
        if (components.length != 3) {
            throw new NoSuchElementException("Invalid formatted line: " + nextLine);
        }
        try {
            int year = Integer.parseInt(components[0]);
            return new Movie(components[2], components[1], year);
        } catch (NumberFormatException e) {
            throw new NoSuchElementException("Invalid formatted line: " + nextLine);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (!open) {
            return;
        }
        reader.close();
        open = false;
    }

}
