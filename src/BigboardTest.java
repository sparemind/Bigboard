import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;


public class BigboardTest {
    /**
     * Word size in bits being used by Bigboard (currently a long).
     */
    private static final int WORD_SIZE = 64;
    /**
     * The number of random tests to run per method.
     */
    private static final int RAND_TESTS = 64;
    /**
     * Maximum dimension a board can be in a random test (inclusive).
     */
    private static final int RAND_SIZE_MAX = 16;
    /**
     * Maximum percent of bits that can be 1 for a board in a random test. Must
     * be in [0, 1.0].
     */
    private static final double RAND_BITS_MAX_PERCENT = 1.0;

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
        int realSize = (int) Math.ceil(((double) actual.size()) / WORD_SIZE) * WORD_SIZE;
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

    public static int[] and(List<Integer> a, List<Integer> b) {
        List<Integer> acc = new ArrayList<>(a);
        acc.retainAll(b);
        return toArr(acc);
    }

    /**
     * Converts a Collection of ints to an array.
     *
     * @param col The Collection to convert to an array.
     * @return An array with the same content as the given Collection.
     */
    public static int[] toArr(Collection<Integer> col) {
        int[] result = new int[col.size()];
        int i = 0;
        for (int value : col) {
            result[i] = value;
            i++;
        }
        return result;
    }

    private static List<Integer> randArr(Random rand, int width, int height) {
        Set<Integer> indices = new TreeSet<>();

        int numIndices = (int) (rand.nextDouble() * RAND_BITS_MAX_PERCENT * width * height);
        int realSize = (int) Math.ceil(((double) width * height) / WORD_SIZE) * WORD_SIZE;
        for (int i = 0; i < numIndices; i++) {
            indices.add(rand.nextInt(realSize));
        }

        return new ArrayList<>(indices);
    }

    @Test
    public void testRandom() {
        Random rand = new Random(0);

        // and(Bigboard)
        for (int i = 0; i < RAND_TESTS; i++) {
            System.out.println("Running and(Bigboard) random test #" + i);
            int width = rand.nextInt(RAND_SIZE_MAX) + 1;
            int height = rand.nextInt(RAND_SIZE_MAX) + 1;
            testAndRandom(rand, width, height);
        }
    }

    private void testAndRandom(Random rand, int width, int height) {
        List<Integer> aIndices = randArr(rand, width, height);
        List<Integer> bIndices = randArr(rand, width, height);
        Bigboard a = makeBB(width, height, toArr(aIndices));
        Bigboard b = makeBB(width, height, toArr(bIndices));

        testEq(and(aIndices, bIndices), a.and(b));
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
    public void testGet() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 3, 70, 168});

        // Get 1 bits
        assertTrue(b0.get(0));
        assertTrue(b0.get(3));
        assertTrue(b0.get(70));
        assertTrue(b0.get(168));
        assertTrue(b0.get(0, 0));
        assertTrue(b0.get(3, 0));
        assertTrue(b0.get(5, 5));
        assertTrue(b0.get(12, 12));

        // Get 0 bits
        assertFalse(b0.get(1));
        assertFalse(b0.get(2));
        assertFalse(b0.get(4));
        assertFalse(b0.get(69));
        assertFalse(b0.get(71));
        assertFalse(b0.get(167));
        assertFalse(b0.get(1, 0));
        assertFalse(b0.get(2, 0));
        assertFalse(b0.get(4, 0));
        assertFalse(b0.get(4, 5));
        assertFalse(b0.get(6, 5));
        assertFalse(b0.get(11, 12));
    }

    @Test
    public void testAnd() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{0, 70, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{1, 27, 159});

        // General case
        testEq(new int[]{0, 70, 168}, b0.and(b1));
        testEq(new int[]{0, 70, 168}, b1.and(b0));
        testEq(new int[]{0}, b0.and(63L));
        // Non-empty AND empty
        testEq(new int[]{}, b0.and(new Bigboard(13, 13)));
        testEq(new int[]{}, new Bigboard(13, 13).and(b0));
        testEq(new int[]{}, b0.and(0L));
        // Disjoint
        testEq(new int[]{}, b1.and(b2));
        testEq(new int[]{}, b2.and(b1));
        testEq(new int[]{}, b0.and(14L));
    }

    @Test
    public void testOr() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{0, 70, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{1, 27, 159});
        Bigboard b3 = makeBB(13, 13, new int[]{15, 70, 155});

        // General case
        testEq(new int[]{0, 10, 15, 70, 155, 160, 168}, b0.or(b3));
        testEq(new int[]{0, 10, 15, 70, 155, 160, 168}, b3.or(b0));
        testEq(new int[]{0, 3, 10, 70, 160, 168}, b0.or(9L));
        // Sub/super set
        testEq(new int[]{0, 10, 70, 160, 168}, b0.or(b1));
        testEq(new int[]{0, 10, 70, 160, 168}, b1.or(b0));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.or(1025L));
        // Non-empty AND empty
        testEq(new int[]{0, 70, 168}, b1.or(new Bigboard(13, 13)));
        testEq(new int[]{0, 70, 168}, new Bigboard(13, 13).or(b1));
        testEq(new int[]{0, 70, 168}, b1.or(0L));
        // Disjoint
        testEq(new int[]{0, 1, 27, 70, 159, 168}, b1.or(b2));
        testEq(new int[]{0, 1, 27, 70, 159, 168}, b2.or(b1));
        testEq(new int[]{0, 1, 2, 3, 70, 168}, b1.or(14L));
    }

    @Test
    public void testXor() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{0, 70, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{1, 27, 159});
        Bigboard b3 = makeBB(13, 13, new int[]{0, 15, 70, 155});

        // General case
        testEq(new int[]{10, 15, 155, 160, 168}, b0.xor(b3));
        testEq(new int[]{10, 15, 155, 160, 168}, b3.xor(b0));
        testEq(new int[]{3, 10, 70, 160, 168}, b0.xor(9L));
        // Sub/super set
        testEq(new int[]{10, 160}, b0.xor(b1));
        testEq(new int[]{10, 160}, b1.xor(b0));
        testEq(new int[]{70, 160, 168}, b0.xor(1025L));
        // Non-empty AND empty
        testEq(new int[]{0, 70, 168}, b1.xor(new Bigboard(13, 13)));
        testEq(new int[]{0, 70, 168}, new Bigboard(13, 13).xor(b1));
        testEq(new int[]{0, 70, 168}, b1.xor(0L));
        // Disjoint
        testEq(new int[]{0, 1, 27, 70, 159, 168}, b1.xor(b2));
        testEq(new int[]{0, 1, 27, 70, 159, 168}, b2.xor(b1));
        testEq(new int[]{0, 1, 2, 3, 70, 168}, b1.xor(14L));
    }

    @Test
    public void testLeft() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{0, 70, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{1, 27, 159});
        Bigboard b3 = makeBB(13, 13, new int[]{0, 15, 70, 155});

        // General case
        testEq(new int[]{10, 20, 80}, b0.left(10));
        testEq(new int[]{13, 83}, b1.left(13));
        testEq(new int[]{8, 34, 166}, b2.left(7));
        testEq(new int[]{12, 27, 82, 167}, b3.left(12));

        // Upper end case
        testEq(new int[]{168}, b0.left(168));
        testEq(new int[]{168}, b1.left(168));
        testEq(new int[]{142, 168}, b2.left(141));
        testEq(new int[]{143}, b2.left(142));
        testEq(new int[]{153, 168}, b3.left(153));
        testEq(new int[]{154}, b3.left(154));

        // Shift everything off the board
        testEq(new int[]{}, b0.left(169));
        testEq(new int[]{}, b1.left(169));
        testEq(new int[]{}, b2.left(169));
        testEq(new int[]{}, b3.left(169));
        testEq(new int[]{}, b0.left(9999));
        testEq(new int[]{}, b1.left(9999));
        testEq(new int[]{}, b2.left(9999));
        testEq(new int[]{}, b3.left(9999));
    }

    @Test
    public void testRight() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{0, 70, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{1, 27, 159});
        Bigboard b3 = makeBB(13, 13, new int[]{0, 15, 70, 155});

        // General case
        testEq(new int[]{0, 60, 150, 158}, b0.right(10));
        testEq(new int[]{57, 155}, b1.right(13));
        testEq(new int[]{20, 152}, b2.right(7));
        testEq(new int[]{3, 58, 143}, b3.right(12));

        // Lower end case
        testEq(new int[]{0}, b0.right(168));
        testEq(new int[]{0}, b1.right(168));
        testEq(new int[]{0, 132}, b2.right(27));
        testEq(new int[]{131}, b2.right(28));
        testEq(new int[]{0, 85}, b3.right(70));
        testEq(new int[]{84}, b3.right(71));

        // Shift everything off the board
        testEq(new int[]{}, b0.right(169));
        testEq(new int[]{}, b1.right(169));
        testEq(new int[]{}, b2.right(169));
        testEq(new int[]{}, b3.right(169));
        testEq(new int[]{}, b0.right(9999));
        testEq(new int[]{}, b1.right(9999));
        testEq(new int[]{}, b2.right(9999));
        testEq(new int[]{}, b3.right(9999));
    }
}