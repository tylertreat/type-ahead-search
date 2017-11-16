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

        private Node<E> left;
        private Node<E> down;
        private Node<E> right;
        private E item;

        Node(char c, E item) {
            this.c = c;
            this.item = item;
        }

    }

    private void insert(String word, Movie movie) {
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
            n = new Node<>(word.charAt(idx), null);
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
                n.item = movie;
            } else {
                Node<Movie> down = insertRec(n.down, word, movie, idx + 1);
                if (n.down == null) {
                    n.down = down;
                }
            }
        }

        return n;
    }

    private List<Movie> prefixMatches(String title) {
        String[] words = title.split(" ");
        Set<Movie> matches = new HashSet<>();
        for (String word : words) {
            matches.addAll(wordPrefixMatches(word));
        }
        return new ArrayList<>(matches);
    }

    private List<Movie> wordPrefixMatches(String word) {
        List<Movie> matches = new ArrayList<>();
        if (word == null || word.length() == 0) {
            return matches;
        }

        Node<Movie> curr = root;
        Node<Movie> last = root;

        if (curr == null) {
            return matches;
        }

        // Descend to the last node in the tree whose prefix matches the word.
        for (int idx = 0; idx < word.length(); idx++) {
            System.out.printf("%d: %s, %s\n", idx, word.charAt(idx), curr.c);
            if (word.charAt(idx) < curr.c) {
                if (curr.left == null) {
                    last = curr;
                    break;
                }
                curr = curr.left;
            } else if (word.charAt(idx) > curr.c) {
                if (curr.right == null) {
                    last = curr;
                    break;
                }
                curr = curr.right;
            } else {
                if (idx + 1 == word.length() || curr.down == null) {
                    last = curr;
                    break;
                }
                curr = curr.down;
            }
        }

        // At this point, any children of the last node are matches.
        traverseRec(last, matches);
        return matches;
    }

    private void traverseRec(Node<Movie> curr, List<Movie> matches) {
        if (curr == null) {
            return;
        }
        if (curr.item != null) {
            matches.add(curr.item);
        }
        traverseRec(curr.left, matches);
        traverseRec(curr.down, matches);
        traverseRec(curr.right, matches);
    }
}
