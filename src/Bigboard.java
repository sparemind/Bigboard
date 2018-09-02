/**
 * MIT License
 * <p>
 * Copyright (c) 2018 Jake Chiang
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * This class represents an immutable bit array data structure where each bit
 * corresponds to a position on a 2D board. Bits are indexed according to a
 * Little-Endian Rank-File mapping, i.e. the least significant bit is mapped to
 * the lower left corner of the board and subsequent bits are mapped across each
 * rank, starting at the first rank. So, a 3x3 board would be indexed as:
 * <pre>
 * 6 7 8
 * 3 4 5
 * 0 1 2
 * </pre>
 *
 * @author Jake Chiang
 * @version 0.1
 */
public class Bigboard {
    // A Bigboard consists of multiple "words" stored in an array. Each word
    // represents a section of the board, starting from the lower left corner.
    // For example, if words were 3 bits, words A, B, and C would represent a
    // 3x3 board as:
    //      C0 C1 C2
    //      B0 B1 B2
    //      A0 A1 A2
    // Where the letter is the word and the number is the bit index of the word.
    //
    // If the board size isn't perfectly divisible by the word size then not all
    // of the bits of the last word will be used. These are the "unused bits."
    //
    //
    // Representation Invariant:
    // - All unused bits are 0

    /**
     * The number of bits needed to address the bits of a word.
     * Currently a word is a long (64 bits), so this is 6 (2^6 = 64).
     */
    private static final int BITS_PER_WORD = 6;
    /**
     * The number of bits that make a word. Equal to 2^BITS_PER_WORD.
     * Currently a word is a long, so this is 64.
     */
    private static final int WORD_SIZE = 1 << BITS_PER_WORD;
    /**
     * A bitmask of BITS_PER_WORD 1's in the least significant bit positions.
     * Currently a word is a long, so this is 0b111111.
     */
    private static final int WORD_SIZE_MASK = WORD_SIZE - 1;

    // The words that represent the board
    private final long[] words;
    // Board width
    private final int width;
    // Board height
    private final int height;
    // Bitmask of the used bits in the final word
    private final long partialSizeMask;

    /**
     * Creates a new bitboard of given width and height.
     *
     * @param width  The width of the board. Must be > 0.
     * @param height The height of the board. Must be > 0.
     */
    public Bigboard(int width, int height) {
        this.width = width;
        this.height = height;
        int size = width * height;
        if ((size & WORD_SIZE_MASK) == 0) {
            // If all words are fully used, then no partial mask is needed.
            this.partialSizeMask = ~0;
        } else {
            this.partialSizeMask = (1L << (size & WORD_SIZE_MASK)) - 1;
        }
        this.words = new long[((size - 1) >> BITS_PER_WORD) + 1];
    }

    /**
     * Constructs a new Bigboard of given width and height equal to another
     * board of the same dimensions represented by a single long. Bits that
     * don't fit on the board are discarded.
     *
     * @param width  The width of the board. Must be > 0.
     * @param height The height of the board. Must be > 0.
     * @param other  The other board that this new Bigboard will equal.
     */
    public Bigboard(int width, int height, long other) {
        this(width, height);
        this.words[0] = other;
        zeroUnusedBits(this);
    }

    /**
     * Constructs a new Bigboard equal to another.
     *
     * @param other The Bigboard which this new Bigboard will equal. Must not be
     *              null.
     */
    public Bigboard(Bigboard other) {
        this.width = other.width;
        this.height = other.height;
        this.partialSizeMask = other.partialSizeMask;
        this.words = new long[other.words.length];
        System.arraycopy(other.words, 0, this.words, 0, this.words.length);
    }

    /**
     * Sets the unused bits of a given board to be 0's.
     *
     * @param board The board to zero out the unused bits of.
     */
    private static void zeroUnusedBits(Bigboard board) {
        board.words[board.words.length - 1] &= board.partialSizeMask;
    }

    /**
     * Returns the width of this board.
     *
     * @return The width of this board in number of positions.
     */
    public int width() {
        return this.width;
    }

    /**
     * Returns the height of this board.
     *
     * @return The height of this board in number of positions.
     */
    public int height() {
        return this.height;
    }

    /**
     * Returns the total number of positions on this board.
     *
     * @return The number of positions on this board.
     */
    public int size() {
        return this.width * this.height;
    }

    /**
     * Sets the bit at the given index on the board to 1. Bits are indexed
     * according to a Little-Endian Rank-File mapping.
     *
     * @param index The index of the position on the board to set to 1. Must be
     *              non-negative and less than board width * height.
     * @return The board that results from setting the bit at the given index
     * on this board to 1.
     */
    public Bigboard set(int index) {
        Bigboard result = new Bigboard(this);
        result.words[index >> BITS_PER_WORD] |= 1L << (index & WORD_SIZE_MASK);
        return result;
    }

    /**
     * Sets the bit at the given index on the board to 0. Bits are indexed
     * according to a Little-Endian Rank-File mapping.
     *
     * @param index The index of the position on the board to set to 0. Must be
     *              non-negative and less than board width * height.
     * @return The board that results from setting the bit at the given index
     * on this board to 0.
     */
    public Bigboard unset(int index) {
        Bigboard result = new Bigboard(this);
        result.words[index >> BITS_PER_WORD] &= ~(1L << (index & WORD_SIZE_MASK));
        return result;
    }

    /**
     * Flips the bit at the given index on the board (i.e. changes it to 0 if it
     * is 1 and changes it to 1 if it is 0). Bits are indexed according to a
     * Little-Endian Rank-File mapping.
     *
     * @param index The index of the position on the board to flip. Must be
     *              non-negative and less than board width * height.
     * @return The board that results from flipping the bit at the given index
     * on this board.
     */
    public Bigboard flip(int index) {
        Bigboard result = new Bigboard(this);
        result.words[index >> BITS_PER_WORD] ^= 1L << (index & WORD_SIZE_MASK);
        return result;
    }

    /**
     * Computes bitwise AND with another board and returns the result.
     *
     * @param other The board to compute bitwise AND with.
     * @return The board that results from computing bitwise AND between this
     * and the other board.
     */
    public Bigboard and(Bigboard other) {
        assert (this.words.length == other.words.length);

        Bigboard result = new Bigboard(this);
        for (int i = 0; i < this.words.length; i++) {
            result.words[i] &= other.words[i];
        }
        return result;
    }

    /**
     * Computes bitwise OR with another board and returns the result.
     *
     * @param other The board to compute bitwise OR with.
     * @return The board that results from computing bitwise OR between this
     * and the other board.
     */
    public Bigboard or(Bigboard other) {
        assert (this.words.length == other.words.length);

        Bigboard result = new Bigboard(this);
        for (int i = 0; i < this.words.length; i++) {
            result.words[i] |= other.words[i];
        }
        return result;
    }

    /**
     * Computes bitwise XOR with another board and returns the result.
     *
     * @param other The board to compute bitwise XOR with.
     * @return The board that results from computing bitwise XOR between this
     * and the other board.
     */
    public Bigboard xor(Bigboard other) {
        assert (this.words.length == other.words.length);

        Bigboard result = new Bigboard(this);
        for (int i = 0; i < this.words.length; i++) {
            result.words[i] ^= other.words[i];
        }
        return result;
    }

    /**
     * Computes a bitwise left shift of a given number of bits and returns the
     * result.
     *
     * @param amount The number of bits to left shift this board. Must be
     *               positive.
     * @return The board that results from bitwise left shifting this board by
     * the given amount.
     */
    public Bigboard left(int amount) {
        Bigboard result = new Bigboard(this);
        if (amount == 0) {
            return result;
        }

        int wordShifts = Math.min(amount >> BITS_PER_WORD, result.words.length);
        // Unsimplified version of the arraycopy version
        // for (int i = result.words.length - 1; i >= wordShifts; i--) {
        //     result.words[i] = result.words[i - wordShifts];
        // }
        System.arraycopy(result.words, 0, result.words, wordShifts, result.words.length -
                wordShifts);
        for (int i = 0; i < wordShifts; i++) {
            result.words[i] = 0;
        }

        int partialShift = amount & WORD_SIZE_MASK;
        if (partialShift != 0) {
            long carry = 0;
            for (int i = wordShifts; i < result.words.length; i++) {
                long newCarry = result.words[i] >>> (WORD_SIZE - partialShift);
                result.words[i] <<= partialShift;
                result.words[i] |= carry;
                carry = newCarry;
            }
        }

        zeroUnusedBits(result);
        return result;
    }

    /**
     * Computes a logical bitwise right shift of a given number of bits and
     * returns the result.
     *
     * @param amount The number of bits to left shift this board. Must be
     *               positive.
     * @return The board that results from bitwise logical right shifting this
     * board by the given amount.
     */
    public Bigboard right(int amount) {
        Bigboard result = new Bigboard(this);
        if (amount == 0) {
            return result;
        }

        int wordShifts = Math.min(amount >> BITS_PER_WORD, result.words.length);
        // Unsimplified version of the arraycopy version
        // for (int i = 0; i < result.words.length - wordShifts; i++) {
        //     result.words[i] = result.words[i + wordShifts];
        // }
        System.arraycopy(result.words, wordShifts, result.words, 0, result.words.length -
                wordShifts);
        for (int i = result.words.length - 1; i >= result.words.length - wordShifts; i--) {
            result.words[i] = 0;
        }

        int partialShift = amount & WORD_SIZE_MASK;
        if (partialShift != 0) {
            long carry = 0;
            for (int i = result.words.length - 1 - wordShifts; i >= 0; i--) {
                long newCarry = result.words[i] << (WORD_SIZE - partialShift);
                result.words[i] >>>= partialShift;
                result.words[i] |= carry;
                carry = newCarry;
            }
        }

        return result;
    }

    /**
     * Computes bitwise AND with another board represented by a single long and
     * returns the result.
     *
     * @param other The board to compute bitwise AND with.
     * @return The board that results from computing bitwise AND between this
     * and the other board.
     */
    public Bigboard and(long other) {
        Bigboard result = new Bigboard(this);
        result.words[0] &= other;
        for (int i = 1; i < result.words.length; i++) {
            result.words[i] = 0;
        }
        zeroUnusedBits(result);
        return result;
    }

    /**
     * Computes bitwise OR with another board represented by a single long and
     * returns the result.
     *
     * @param other The board to compute bitwise OR with.
     * @return The board that results from computing bitwise OR between this
     * and the other board.
     */
    public Bigboard or(long other) {
        Bigboard result = new Bigboard(this);
        result.words[0] |= other;
        zeroUnusedBits(result);
        return result;
    }

    /**
     * Computes bitwise XOR with another board represented by a single long and
     * returns the result.
     *
     * @param other The board to compute bitwise XOR with.
     * @return The board that results from computing bitwise XOR between this
     * and the other board.
     */
    public Bigboard xor(long other) {
        Bigboard result = new Bigboard(this);
        result.words[0] ^= other;
        zeroUnusedBits(result);
        return result;
    }

    /**
     * Computes the bitwise NOT of this board and returns the result as a new
     * board.
     *
     * @return The bitwise NOT of this board.
     */
    public Bigboard not() {
        Bigboard result = new Bigboard(this);
        for (int i = 0; i < result.words.length; i++) {
            result.words[i] = ~result.words[i];
        }
        zeroUnusedBits(result);
        return result;
    }

    /**
     * Returns the bit at the given index on the board. Bits are indexed
     * according to a Little-Endian Rank-File mapping.
     *
     * @param index The index of the position on the board to get the bit of.
     *              Must be non-negative and less than board width * height.
     * @return True if the bit at the position is 1, false if it is 0.
     */
    public boolean get(int index) {
        return (this.words[index >> BITS_PER_WORD] & (1L << index)) != 0;
    }

    /**
     * Returns the bit at the given position on the board, as specified by its
     * xy-coordinates. Positions are indexed with (0,0) as the lower left
     * corner of the board and (width - 1, height - 1) as the upper right.
     *
     * @param x The x-coordinate of the position on the board to get the bit of.
     *          Must be non-negative and less than the board width.
     * @param y The y-coordinate of the position on the board to get the bit of.
     *          Must be non-negative and less than the board height.
     * @return True if the bit at the position is 1, false if it is 0.
     */
    public boolean get(int x, int y) {
        return get(y * this.width + x);
    }

    /**
     * Returns a String depicting the bits in their board positions. 0 bits are
     * represented by '.', 1 bits are represented by 'X'. The board is
     * constructed from the bits in Little-Endian Rank-File mapping.
     * <p>
     * For example, a 4x4 board for the binary value of 25 would be:
     * . . . .
     * . . . .
     * X . . .
     * X . . X
     *
     * @return A representation of the 2D board showing 0 bits as '.' and 1 bits
     * as 'X'.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int y = this.height - 1; y >= 0; y--) {
            for (int x = 0; x < this.width; x++) {
                if (get(y * this.width + x)) {
                    sb.append('X');
                } else {
                    sb.append('.');
                }
                sb.append(' ');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;

        result = result * PRIME + Integer.hashCode(this.width);
        result = result * PRIME + Integer.hashCode(this.height);
        for (long word : this.words) {
            result = result * PRIME + Long.hashCode(word);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Two boards are considered equal if they are the same dimensions and all
     * bits are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Bigboard)) {
            return false;
        }
        if (o == this) {
            return true;
        }

        Bigboard other = (Bigboard) o;
        if (this.width != other.width || this.height != other.height) {
            return false;
        }

        for (int i = 0; i < this.words.length; i++) {
            if (this.words[i] != other.words[i]) {
                return false;
            }
        }
        return true;
    }
}
