import java.util.function.BiFunction;

public class BigboardPerformanceTest {
    // Width/height sizes of the boards to test
    public static final int[] SIZES = {3, 8, 13, 20, 100};
    // Number of operate iterations to do before timing (for JVM warmup)
    public static final int WARMUP_ITERATIONS = (int) 1e7;
    // Number of operate iterations to do per timing session
    public static final int ITERATIONS = (int) 1e7;
    // The ordered results table column headers
    public static final String[] TABLE_HEADERS = {"Board Size", "AND", "OR", "XOR", "LEFT",
            "RIGHT", "Average", "Avg. ops/ms"};
    // Nanoseconds per second
    private static final int NS_PER_S = (int) 1e9;

    public static void main(String[] args) {
        double[] results = new double[TABLE_HEADERS.length - 1];

        // Print extra information about tests
        System.out.println(ITERATIONS + " iterations per operation");
        System.out.println(WARMUP_ITERATIONS + " warmup iterations");
        System.out.println();

        // Print table column headers
        StringBuilder sb = new StringBuilder();
        for (String header : TABLE_HEADERS) {
            sb.append(String.format("%-13s", header));
        }
        System.out.println(sb.toString());
        // Print header/data dividing line
        for (int i = 0; i < sb.length(); i++) {
            System.out.print("=");
        }
        System.out.println();

        // Do tests and print results
        for (int size : SIZES) {
            results[0] = test(size, size, Bigboard::and);
            results[1] = test(size, size, Bigboard::or);
            results[2] = test(size, size, Bigboard::xor);
            results[3] = test(size, size, Bigboard::left);
            results[4] = test(size, size, Bigboard::right);

            // Compute average
            for (int i = 0; i < results.length - 2; i++) {
                results[5] += results[i];
            }
            results[5] /= results.length - 2;
            // Determine average number of operations per millisecond
            results[6] = ITERATIONS / results[5] / 1000;

            printResults(size, results);
        }
    }

    /**
     * Prints a series of formatted columns for each entry in an array of
     * time results.
     *
     * @param size    Dimension of the board these results are for.
     * @param results The times in seconds each test for this board size took to
     *                complete.
     */
    private static void printResults(int size, double[] results) {
        StringBuilder sb = new StringBuilder();
        for (double time : results) {
            sb.append(String.format("%-13.5f", time));
        }

        String boardSize = String.format("%dx%d", size, size);
        System.out.printf("%-13s%s\n", boardSize, sb.toString());
    }

    /**
     * Executes an operation on board of given dimensions for the set number of
     * iterations and returns the execution time.
     *
     * @param width  Width of the board to test.
     * @param height Height of the board to test.
     * @param op     An operation to test. Accepts a board and applies an int
     *               argument in some way to produce a new resulting board.
     * @return The time it took in seconds to run the given operation the set
     * number of iterations.
     */
    public static double test(int width, int height, BiFunction<Bigboard, Integer, Bigboard> op) {
        Bigboard board = new Bigboard(width, height);

        // Iterations to get JVM warmed up
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            board = op.apply(board, i);
        }

        // Actual test
        long start = System.nanoTime();
        for (int i = 0; i < BigboardPerformanceTest.ITERATIONS; i++) {
            board = op.apply(board, i);
        }
        long time = System.nanoTime() - start;

        // Convert nanoseconds to seconds and return
        return ((double) time) / NS_PER_S;
    }
}
