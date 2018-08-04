import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class BigboardTest {
    private static final int ITERATIONS = 64;
    private static final int SHIFT = 8;

    // Pseudorandom boards
    private static Bigboard rand13x13;
    // Testing boards
    private static Bigboard bb13x13;

    @BeforeAll
    public static void initialize() {
        rand13x13 = generatePseudorandom(13, 13);

        bb13x13 = new Bigboard(13, 13, 7000L);
        bb13x13 = bb13x13.left(13 * 12 - 3).or(7000L);
    }

    /**
     * Generates a random looking board of a given width and height. The board
     * will always be the same for the given dimensions.
     *
     * @param width  The width of the board to generate.
     * @param height The height of the board to generate.
     * @return A random looking board of the given width and height.
     */
    private static Bigboard generatePseudorandom(int width, int height) {
        Bigboard b = new Bigboard(width, height);

        for (int i = 0; i < ITERATIONS; i++) {
            b = b.left(SHIFT).xor(i);
        }

        return b;
    }

    /**
     * Makes a board of a given width and height with 1 bits at specified
     * indices.
     *
     * @param width   The width of the board to make.
     * @param height  The height of the board to make
     * @param indices The indices of the bits on the board to be 1's.
     * @return A new board of the given width and height with all bits as 0
     * except for the bits specified by the index array, which are 1's.
     */
    private static Bigboard makeBB(int width, int height, int[] indices) {
        Bigboard b = new Bigboard(width, height);
        for (int i : indices) {
            Bigboard bit = new Bigboard(width, height, 1L);
            b = b.or(bit.left(i));
        }
        return b;
    }

    /**
     * Asserts that the only 1 bits in a given board match an array of indices
     * where 1 bits are expected. Also asserts that indices not appearing in the
     * array are 0 bits and that all unused bits are 0.
     *
     * @param expectedIndices The index positions that 1 bits are expected, in
     *                        ascending order. Indices not appearing in this
     *                        array are expected to be 0 bits.
     * @param actual          The board to test.
     */
    private static void testEq(int[] expectedIndices, Bigboard actual) {
        int i = 0;

        // Get total size of the board (including unused bits).
        int realSize = (int) Math.ceil(actual.size() / 64.0) * 64;
        for (int j = 0; j < realSize; j++) {
            if (j >= actual.size()) {
                // Assert that all unused bits are 0
                assertFalse(actual.get(j), makeError(0, j, actual));
            } else if (i < expectedIndices.length && expectedIndices[i] == j) {
                // Assert that 1 bits appear where they are expected
                assertTrue(actual.get(j), makeError(1, j, actual));
                i++;
            } else {
                // Assert that 0 bits appear where they are expected
                assertFalse(actual.get(j), makeError(0, j, actual));
            }
        }
    }

    /**
     * Creates an error message string for a bit at some index being incorrect
     * for some board.
     * <p>
     * The message consists of a description of which bit was
     * expected and which was found, the index and xy-coordinates of the
     * incorrect bit, and a string representation of the incorrect board.
     *
     * @param expected The expected bit, either 0 or 1.
     * @param index    The index where the bit is incorrect.
     * @param board    The board with the incorrect bit.
     * @return An error message describing the incorrect bit at the given index,
     * along with the index's xy-coordinates and a string representation of the
     * board.
     */
    private static String makeError(int expected, int index, Bigboard board) {
        int x = index % board.width();
        int y = index / board.width();

        StringBuilder sb = new StringBuilder("Expected ");
        sb.append(expected).append(" at position ").append(index);
        sb.append(" (").append(x).append(',').append(y).append(")");
        sb.append(", ").append(1 - expected).append(" instead.");
        sb.append('\n').append(board.toString());

        return sb.toString();
    }

    @Test
    public void testConstructor() {
        // Create many board of different dimensions
        for (int x = 1; x < 32; x++) {
            for (int y = 1; y < 32; y++) {
                Bigboard b = new Bigboard(x, y);
            }
        }

        // Test copy constructor
        for (int i = 1; i < 64; i++) {
            Bigboard b1 = new Bigboard(8, 8).or(i);
            Bigboard b2 = new Bigboard(b1);
            assertEquals(b1, b2);
        }

        // Check that unused bits aren't set
        Bigboard b1 = new Bigboard(2, 2, 32);
        Bigboard b2 = new Bigboard(2, 2);
        assertEquals(b1, b2);
        b1 = b1.right(1);
        assertEquals(b1, b2);
    }

    @Test
    public void testAnd() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{0, 70, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{1, 27, 159});

        // General case
        testEq(new int[]{0, 70, 168}, b0.and(b1));
        testEq(new int[]{0, 70, 168}, b1.and(b0));
        // Non-empty AND empty
        testEq(new int[]{}, b0.and(new Bigboard(13, 13)));
        testEq(new int[]{}, new Bigboard(13, 13).and(b0));
        // Disjoint
        testEq(new int[]{}, b1.and(b2));
        testEq(new int[]{}, b2.and(b1));
    }
}