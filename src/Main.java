import java.util.Scanner;

public class Main {

    private static final String QUIT = "quit";
    private static final String PROCESS_FILE = "process-file";
    private static final String QUERY = "query";
    private static final MovieIndex INDEX = new TernaryTreeMovieIndex();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            String line = input.nextLine();
            String[] directiveAndArg = line.split(" ", 2);
            String arg = null;
            if (directiveAndArg.length == 2) {
                arg = directiveAndArg[1];
            }
            System.out.println(line);
            processDirective(directiveAndArg[0], arg);
        }
    }

    private static void processDirective(String directive, String arg) {
        switch (directive) {
            case QUIT:
                System.exit(0);
            case PROCESS_FILE:
                INDEX.index(new MovieFileReader(arg));
                return;
            case QUERY:
                // TODO
                throw new RuntimeException("not implemented");
            default:
                throw new IllegalArgumentException("Invalid directive: " + directive);
        }
    }
}
