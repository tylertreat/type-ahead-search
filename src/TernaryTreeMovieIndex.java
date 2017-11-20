import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code TernaryTreeMovieIndex} implements the {@link MovieIndex} interface by using a fixed-size thread pool to
 * asynchronously index Movies and storing them in a ternary search tree.
 */
public class TernaryTreeMovieIndex implements MovieIndex {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));

    private Node<Movie> root;

    @Override
    public void index(MovieReader reader) {
        executor.execute(() -> {
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
        private final Set<E> items = new HashSet<>(0);

        private Node<E> left;
        private Node<E> down;
        private Node<E> right;

        Node(char c) {
            this.c = c;
        }

    }

    private synchronized void insert(String word, Movie movie) {
        if (word == null || word.length() == 0) {
            return;
        }
        Node<Movie> n = insertRec(root, word, movie, 0);
        if (root == null) {
            root = n;
        }

    }

    private Node<Movie> insertRec(Node<Movie> n, String word, Movie movie, int idx) {
        if (n == null) {
            n = new Node<>(word.charAt(idx));
        }

        if (word.charAt(idx) < n.c) {
            Node<Movie> left = insertRec(n.left, word, movie, idx);
            if (n.left == null) {
                n.left = left;
            }
        } else if (word.charAt(idx) > n.c) {
            Node<Movie> right = insertRec(n.right, word, movie, idx);
            if (n.right == null) {
                n.right = right;
            }
        } else {
            if (idx + 1 == word.length()) {
                n.items.add(movie);
            } else {
                Node<Movie> down = insertRec(n.down, word, movie, idx + 1);
                if (n.down == null) {
                    n.down = down;
                }
            }
        }

        return n;
    }

    private synchronized List<Movie> prefixMatches(String query) {
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
        // Galaxy" and "Rise of the Guardians" since both words are contained in the titles but in different orders.
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

        Node<Movie> curr = root;
        Node<Movie> last = null;
        int idx = 0;

        if (curr == null) {
            return matches;
        }

        // Descend to the last node in the tree whose prefix matches the word.
        while (true) {
            if (word.charAt(idx) < curr.c) {
                if (curr.left == null) {
                    break;
                }
                curr = curr.left;
            } else if (word.charAt(idx) > curr.c) {
                if (curr.right == null) {
                    break;
                }
                curr = curr.right;
            } else {
                if (++idx == word.length() || curr.down == null) {
                    if (idx == word.length()) {
                        last = curr;
                    }
                    break;
                }
                curr = curr.down;
            }
        }

        if (last == null) {
            return matches;
        } else if (!last.items.isEmpty()) {
            matches.addAll(last.items);
        }
        // At this point, follow all children of the "down" node to find prefix matches.
        traverseRec(last.down, matches);
        return matches;
    }

    private void traverseRec(Node<Movie> curr, Set<Movie> matches) {
        if (curr == null) {
            return;
        }
        if (!curr.items.isEmpty()) {
            matches.addAll(curr.items);
        }
        traverseRec(curr.left, matches);
        traverseRec(curr.down, matches);
        traverseRec(curr.right, matches);
    }
}
