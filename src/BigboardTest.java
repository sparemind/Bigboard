import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;


public class BigboardTest {
    /**
     * Word size in bits being used by Bigboard (currently a long).
     */
    private static final int WORD_SIZE = 64;
    /**
     * The number of random tests to run per method.
     */
    private static final int RAND_TESTS = 256;
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

    /**
     * Calculates the intersection of two lists and returns the result as an
     * array.
     *
     * @param a The first list to intersect with the second.
     * @param b The second list to intersect with the first.
     * @return The intersection of the two given lists as a new array.
     */
    public static int[] and(List<Integer> a, List<Integer> b) {
        List<Integer> acc = new ArrayList<>(a);
        acc.retainAll(b);
        return toArr(acc);
    }

    /**
     * Calculates the union of two lists and returns the result as an array.
     *
     * @param a The first list to union with the second.
     * @param b The second list to union with the first.
     * @return The union of the two given lists as a new array.
     */
    public static int[] or(List<Integer> a, List<Integer> b) {
        Set<Integer> acc = new TreeSet<>(a);
        acc.addAll(b);
        return toArr(acc);
    }

    /**
     * Calculates the exclusive disjunction (XOR) of two lists and returns the
     * result as an array.
     *
     * @param a The first list to XOR with the second.
     * @param b The second list to XOR with the first.
     * @return The XOR of the two given lists as a new array.
     */
    public static int[] xor(List<Integer> a, List<Integer> b) {
        Set<Integer> acc = new TreeSet<>(a);
        acc.addAll(b);
        acc.removeIf(next -> a.contains(next) && b.contains(next));
        return toArr(acc);
    }

    /**
     * Adds a given amount to every value in a list and returns the result as an
     * array. This operations represents a bitwise left-shift for a list of
     * indices of 1 bits.
     *
     * @param a The list for which to add "b" to every element.
     * @param b The amount to add to every element.
     * @return An array of all items in the given list, in the same order, but
     * with the amount "b" added.
     */
    public static int[] left(List<Integer> a, int b) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) + b);
        }
        return toArr(result);
    }

    /**
     * Subtracts a given amount to every value in a list and returns the result
     * as an array. If any value is negative, it is removed from the final list.
     * This operation represents a bitwise right-shift for a list of indices of
     * 1 bits.
     *
     * @param a The list for which to subtract "b" from every element.
     * @param b The amount to subtract from every element.
     * @return An array of all items in the given list, in the same order, but
     * with the amount "b" subtracted. Any negative values are removed from the
     * final list.
     */
    public static int[] right(List<Integer> a, int b) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            int newValue = a.get(i) - b;
            if (newValue >= 0) {
                result.add(newValue);
            }
        }
        return toArr(result);
    }

    /**
     * Returns the first element in a given list, if one exists. This operation
     * represents getting the least significant 1 bit (LS1B/LSB) for a list of
     * indices of 1 bits.
     *
     * @param a The list to get the first element from.
     * @return A single element array of the first element in the given list. If
     * the given list is empty, this array will be empty.
     */
    public static int[] lsb(List<Integer> a) {
        if (a.isEmpty()) {
            return new int[0];
        } else {
            int[] result = new int[1];
            result[0] = a.get(0);
            return result;
        }
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

    /**
     * Creates a sorted list of indices for a board of given width and height.
     *
     * @param rand   The random number generator to use to produce the indices.
     * @param width  The width of the board to create the indices for.
     * @param height The height of the board to create the indices for.
     * @return A sorted list of indices for a board of the given width and
     * height. Index values range from 0 to the index of the highest bit being
     * used in the board, inclusive (even if this upper bound specifies an
     * index not on the actual board).
     */
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
    public void randTestAnd() {
        System.out.println("Testing and():");
        randTestBBBFunc(BigboardTest::and, Bigboard::and);
    }

    @Test
    public void randTestOr() {
        System.out.println("Testing or():");
        randTestBBBFunc(BigboardTest::or, Bigboard::or);
    }

    @Test
    public void randTestXor() {
        System.out.println("Testing xor():");
        randTestBBBFunc(BigboardTest::xor, Bigboard::xor);
    }

    @Test
    public void randTestLeft() {
        System.out.println("Testing left():");
        randTestBIBFunc(BigboardTest::left, Bigboard::left);
    }

    @Test
    public void randTestRight() {
        System.out.println("Testing right():");
        randTestBIBFunc(BigboardTest::right, Bigboard::right);
    }

    @Test
    public void randTestLsb() {
        System.out.println("Testing lsb():");
        randTestBBFunc(BigboardTest::lsb, Bigboard::lsb);
    }

    /**
     * Tests a binary Bigboard function that accepts a Bigboard, another
     * Bigboard, and returns a Bigboard (function signature abbreviated BBB) for
     * a series of random boards. Actual results are compared against expected
     * results generated by a separate function that produces its results using
     * only the indices of the set bits in the boards being given to the tested
     * function.
     * <p>
     * Number of tests is determined by class constants.
     *
     * @param expectedFunc The function to generate the expected results with.
     *                     Accepts two lists of indices of set bits in a board
     *                     and returns an array of indices that should be set in
     *                     the board resulting from the Bigboard operation.
     *                     Indices in result must be in ascending order.
     * @param func         The Bigboard operation to test. Accepts two Bigboards
     *                     and returns a third.
     */
    private void randTestBBBFunc(BiFunction<List<Integer>, List<Integer>, int[]> expectedFunc,
                                 BiFunction<Bigboard, Bigboard, Bigboard> func) {
        Random rand = new Random(0);

        for (int i = 0; i < RAND_TESTS; i++) {
            System.out.println("\tRandom test #" + i);
            int width = rand.nextInt(RAND_SIZE_MAX) + 1;
            int height = rand.nextInt(RAND_SIZE_MAX) + 1;

            List<Integer> aIndices = randArr(rand, width, height);
            List<Integer> bIndices = randArr(rand, width, height);
            Bigboard a = makeBB(width, height, toArr(aIndices));
            Bigboard b = makeBB(width, height, toArr(bIndices));

            testEq(expectedFunc.apply(aIndices, bIndices), func.apply(a, b));
        }
    }

    /**
     * Tests a binary Bigboard function that accepts a Bigboard, an Integer, and
     * returns a Bigboard (function signature abbreviated BIB) for a series of
     * random boards. Actual results are compared against expected results
     * generated by a separate function that produces its results using only the
     * indices of the set bits in the boards being given to the tested function.
     * <p>
     * Number of tests is determined by class constants.
     *
     * @param expectedFunc The function to generate the expected results with.
     *                     Accepts a list of indices of set bits in a board and
     *                     an integer parameter with which to modify them in
     *                     some way and returns an array of indices that should
     *                     be set in the board resulting from the Bigboard
     *                     operation. Indices in result must be in ascending
     *                     order.
     * @param func         The Bigboard operation to test. Accepts a Bigboard
     *                     and an integer and returns a new Bigboard.
     */
    private void randTestBIBFunc(BiFunction<List<Integer>, Integer, int[]> expectedFunc,
                                 BiFunction<Bigboard, Integer, Bigboard> func) {
        Random rand = new Random(0);

        for (int i = 0; i < RAND_TESTS; i++) {
            System.out.println("\tRandom test #" + i);
            int width = rand.nextInt(RAND_SIZE_MAX) + 1;
            int height = rand.nextInt(RAND_SIZE_MAX) + 1;

            List<Integer> aIndices = randArr(rand, width, height);
            Bigboard a = makeBB(width, height, toArr(aIndices));
            int realSize = (int) Math.ceil(((double) width * height) / WORD_SIZE) * WORD_SIZE;
            int b = rand.nextInt(realSize);
            aIndices.removeIf(integer -> integer >= width * height);

            testEq(expectedFunc.apply(aIndices, b), func.apply(a, b));
        }
    }

    /**
     * Tests a unary Bigboard function that accepts a Bigboard and returns a
     * Bigboard (function signature abbreviated BB) for a series of random
     * boards. Actual results are compared against expected results generated by
     * a separate function that produces its results using only the indices of
     * the set bits in the boards being given to the tested function.
     * <p>
     * Number of tests is determined by class constants.
     *
     * @param expectedFunc The function to generate the expected results with.
     *                     Accepts a list of indices of set bits in a board and
     *                     returns an array of indices that should be set in the
     *                     board resulting from the Bigboard operation. Indices
     *                     in result must be in ascending order.
     * @param func         The Bigboard operation to test. Accepts a Bigboard
     *                     and an integer and returns a new Bigboard.
     */
    private void randTestBBFunc(Function<List<Integer>, int[]> expectedFunc, Function<Bigboard,
            Bigboard> func) {
        Random rand = new Random(0);

        for (int i = 0; i < RAND_TESTS; i++) {
            System.out.println("\tRandom test #" + i);
            int width = rand.nextInt(RAND_SIZE_MAX) + 1;
            int height = rand.nextInt(RAND_SIZE_MAX) + 1;

            List<Integer> aIndices = randArr(rand, width, height);
            Bigboard a = makeBB(width, height, toArr(aIndices));
            aIndices.removeIf(integer -> integer >= width * height);

            testEq(expectedFunc.apply(aIndices), func.apply(a));
        }
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

    @Test
    public void testNot() {
        Bigboard b0 = makeBB(3, 3, new int[]{0, 3, 5, 8});
        Bigboard b1 = makeBB(3, 3, new int[]{});
        Bigboard b2 = makeBB(1, 1, new int[]{});

        testEq(new int[]{1, 2, 4, 6, 7}, b0.not());
        testEq(new int[]{0, 3, 5, 8}, b0.not().not());

        testEq(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, b1.not());
        testEq(new int[]{}, b1.not().not());

        testEq(new int[]{0}, b2.not());
        testEq(new int[]{}, b2.not().not());
    }

    @Test
    public void testSet() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{10, 70, 160});

        // Set an already set bit
        testEq(new int[]{0, 10, 70, 160, 168}, b0.set(0));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.set(10));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.set(70));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.set(160));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.set(168));

        // Set an unset bit
        testEq(new int[]{0, 10, 70, 160}, b1.set(0));
        testEq(new int[]{10, 70, 160, 168}, b1.set(168));
        testEq(new int[]{1, 10, 70, 160}, b1.set(1));
        testEq(new int[]{10, 70, 127, 160}, b1.set(127));
        testEq(new int[]{10, 70, 128, 160}, b1.set(128));
        testEq(new int[]{10, 64, 70, 160}, b1.set(64));
    }

    @Test
    public void testUnset() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});

        // Unset an already unset bit
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(1));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(9));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(11));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(64));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(127));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(128));
        testEq(new int[]{0, 10, 70, 160, 168}, b0.unset(167));

        // Unset a set bit
        testEq(new int[]{10, 70, 160, 168}, b0.unset(0));
        testEq(new int[]{0, 70, 160, 168}, b0.unset(10));
        testEq(new int[]{0, 10, 160, 168}, b0.unset(70));
        testEq(new int[]{0, 10, 70, 168}, b0.unset(160));
        testEq(new int[]{0, 10, 70, 160}, b0.unset(168));
    }

    @Test
    public void testFlip() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{10, 70, 160});

        // Flip a set bit
        testEq(new int[]{10, 70, 160, 168}, b0.flip(0));
        testEq(new int[]{0, 70, 160, 168}, b0.flip(10));
        testEq(new int[]{0, 10, 160, 168}, b0.flip(70));
        testEq(new int[]{0, 10, 70, 168}, b0.flip(160));
        testEq(new int[]{0, 10, 70, 160}, b0.flip(168));

        // Flip an unset bit
        testEq(new int[]{0, 10, 70, 160}, b1.flip(0));
        testEq(new int[]{10, 70, 160, 168}, b1.flip(168));
        testEq(new int[]{1, 10, 70, 160}, b1.flip(1));
        testEq(new int[]{10, 70, 127, 160}, b1.flip(127));
        testEq(new int[]{10, 70, 128, 160}, b1.flip(128));
        testEq(new int[]{10, 64, 70, 160}, b1.flip(64));
    }

    @Test
    public void testLsb() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{10, 70, 160, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{70, 160, 168});
        Bigboard b3 = makeBB(13, 13, new int[]{160, 168});
        Bigboard b4 = makeBB(13, 13, new int[]{168});
        Bigboard b5 = makeBB(13, 13, new int[]{});

        testEq(new int[]{0}, b0.lsb());
        testEq(new int[]{10}, b1.lsb());
        testEq(new int[]{70}, b2.lsb());
        testEq(new int[]{160}, b3.lsb());
        testEq(new int[]{168}, b4.lsb());
        testEq(new int[]{}, b5.lsb());
    }

    @Test
    public void testBitScanForward() {
        Bigboard b0 = makeBB(13, 13, new int[]{0, 10, 70, 160, 168});
        Bigboard b1 = makeBB(13, 13, new int[]{10, 70, 160, 168});
        Bigboard b2 = makeBB(13, 13, new int[]{70, 160, 168});
        Bigboard b3 = makeBB(13, 13, new int[]{160, 168});
        Bigboard b4 = makeBB(13, 13, new int[]{168});
        Bigboard b5 = makeBB(13, 13, new int[]{});

        assertEquals(0, b0.bitScanForward());
        assertEquals(10, b1.bitScanForward());
        assertEquals(70, b2.bitScanForward());
        assertEquals(160, b3.bitScanForward());
        assertEquals(168, b4.bitScanForward());
        assertEquals(-1, b5.bitScanForward());
    }
}