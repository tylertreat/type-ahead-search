import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@code TernaryTreeMovieIndex} implements the {@link MovieIndex} interface by using a fixed-size thread pool to
 * asynchronously index Movies and storing them in a ternary search tree. This is thread-safe in that multiple indexing
 * operations can happen concurrently as well as queries.
 */
public class TernaryTreeMovieIndex implements MovieIndex {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));

    private final AtomicReference<Node<Movie>> root = new AtomicReference<>();

    @Override
    public Future<?> index(MovieReader reader) {
        return executor.submit(() -> {
            try (reader) {
                reader.open();
                while (reader.hasNext()) {
                    Movie movie = reader.next();
                    // Index each word in the title.
                    for (String word : movie.getTitle().split(" ")) {
                        insert(word.toLowerCase(), movie); // Normalize to lower case.
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<Movie> query(String query, int limit) {
        List<Movie> matches = prefixMatches(query.toLowerCase()); // Normalize to lower case.
        matches.sort(Comparator.comparing(Movie::getTitle));
        return matches.subList(0, Math.min(limit, matches.size()));
    }

    private class Node<E> {

        private final char c;
        private final Set<E> items = Collections.synchronizedSet(new HashSet<>(0));

        private final AtomicReference<Node<E>> left = new AtomicReference<>();
        private final AtomicReference<Node<E>> down = new AtomicReference<>();
        private final AtomicReference<Node<E>> right = new AtomicReference<>();

        Node(char c) {
            this.c = c;
        }

    }

    /**
     * Inserts a word into the tree for the given {@code Movie}.
     */
    private void insert(String word, Movie movie) {
        if (word == null || word.length() == 0) {
            return;
        }
        insertRec(root, word, movie, 0);
    }

    private void insertRec(AtomicReference<Node<Movie>> ref, String word, Movie movie, int idx) {
        Node<Movie> old;
        Node<Movie> n;
        do {
            old = ref.get();
            n = old;

            if (n == null) {
                n = new Node<>(word.charAt(idx));
            }

            if (word.charAt(idx) < n.c) {
                insertRec(n.left, word, movie, idx);
            } else if (word.charAt(idx) > n.c) {
                insertRec(n.right, word, movie, idx);
            } else {
                if (idx + 1 == word.length()) {
                    n.items.add(movie);
                } else {
                    insertRec(n.down, word, movie, idx + 1);
                }
            }
        } while (!ref.compareAndSet(old, n));
    }

    /**
     * Performs a prefix-match lookup for the given query on the tree.
     */
    private List<Movie> prefixMatches(String query) {
        // Prefix match on each word in the query.
        String[] words = query.split(" ");
        @SuppressWarnings({"unchecked"})
        Set<Movie>[] matches = new Set[words.length];
        for (int i = 0; i < words.length; i++) {
            matches[i] = wordPrefixMatches(words[i]);
        }
        if (matches.length == 0) {
            return new ArrayList<>(0);
        }

        // Take the intersection of the prefix matches, e.g. query "part II" should match
        // "The Hangover Part II" but not "Harry Potter and the Deathly Hollows Part 1". NOTE: this may not be entirely
        // correct depending on desired behavior. For example, query "guardians of" matches both "Guardians of the
        // Galaxy" and "Rise of the Guardians" since both words are contained in the titles but in different orders. We
        // do it this way for now since we want to allow prefix matching of any words in the title.
        Set<Movie> intersection = matches[0];
        for (int i = 1; i < matches.length; i++) {
            intersection.retainAll(matches[i]);
        }
        return new ArrayList<>(intersection);
    }

    private Set<Movie> wordPrefixMatches(String word) {
        Set<Movie> matches = new HashSet<>();
        if (word == null || word.length() == 0) {
            return matches;
        }

        Node<Movie> curr = root.get();
        Node<Movie> last = null;
        int idx = 0;

        if (curr == null) {
            return matches;
        }

        // Descend to the last node in the tree whose prefix matches the word.
        while (true) {
            if (word.charAt(idx) < curr.c) {
                Node<Movie> left = curr.left.get();
                if (left == null) {
                    break;
                }
                curr = left;
            } else if (word.charAt(idx) > curr.c) {
                Node<Movie> right = curr.right.get();
                if (right == null) {
                    break;
                }
                curr = right;
            } else {
                Node<Movie> down = curr.down.get();
                if (++idx == word.length() || down == null) {
                    if (idx == word.length()) {
                        last = curr;
                    }
                    break;
                }
                curr = down;
            }
        }

        if (last == null) {
            return matches;
        } else if (!last.items.isEmpty()) {
            matches.addAll(last.items);
        }
        // At this point, follow all children of the "down" node to find prefix matches.
        traverseRec(last.down.get(), matches);
        return matches;
    }

    private void traverseRec(Node<Movie> curr, Set<Movie> matches) {
        if (curr == null) {
            return;
        }
        if (!curr.items.isEmpty()) {
            matches.addAll(curr.items);
        }
        traverseRec(curr.left.get(), matches);
        traverseRec(curr.down.get(), matches);
        traverseRec(curr.right.get(), matches);
    }
}
