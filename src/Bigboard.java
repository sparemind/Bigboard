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
 * rank, starting at the first rank. A 3x3 board would be indexed as:
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
    /**
     * The number of bits needed to address the bits of a word. Currently a word
     * is a long (64 bits), so this is 6 (2^6 = 64).
     */
    private static final int BITS_PER_WORD = 6;
    /**
     * The number of bits that make a word. Equal to 2^BITS_PER_WORD.
     */
    private static final int WORD_SIZE = 1 << BITS_PER_WORD;
    /**
     * A bitmask of BITS_PER_WORD 1's in the least significant bit positions.
     */
    private static final int WORD_SIZE_MASK = WORD_SIZE - 1;

    // The words that represent the board
    private final long[] words;
    // Board width
    private final int width;
    // Board height
    private final int height;
    private final int remainder;

    public Bigboard(int width, int height) {
        this.width = width;
        this.height = height;
        int size = width * height;
        this.remainder = size & WORD_SIZE_MASK;
        this.words = new long[((size - 1) >> BITS_PER_WORD) + 1];
    }

    public Bigboard(Bigboard other) {
        this.width = other.width;
        this.height = other.height;
        this.remainder = other.remainder;
        this.words = new long[other.words.length];
        System.arraycopy(other.words, 0, this.words, 0, this.words.length);
    }

    public Bigboard and(Bigboard other) {
        assert (this.words.length == other.words.length);

        Bigboard result = new Bigboard(this);
        for (int i = 0; i < this.words.length; i++) {
            result.words[i] &= other.words[i];
        }
        return result;
    }

    public Bigboard or(Bigboard other) {
        assert (this.words.length == other.words.length);

        Bigboard result = new Bigboard(this);
        for (int i = 0; i < this.words.length; i++) {
            result.words[i] |= other.words[i];
        }
        return result;
    }

    public Bigboard xor(Bigboard other) {
        assert (this.words.length == other.words.length);

        Bigboard result = new Bigboard(this);
        for (int i = 0; i < this.words.length; i++) {
            result.words[i] ^= other.words[i];
        }
        return result;
    }

    public Bigboard left(int amount) {
        Bigboard result = new Bigboard(this);

        int wordShifts = Math.min(amount >> BITS_PER_WORD, result.words.length);
        for (int i = 0; i < result.words.length - wordShifts; i++) {
            result.words[i + wordShifts] = result.words[i];
        }
        for (int i = 0; i < wordShifts; i++) {
            result.words[i] = 0;
        }

        int partialShift = amount & WORD_SIZE_MASK;
        long carry = 0;
        for (int i = 0; i < result.words.length; i++) {
            long newCarry = result.words[i] >>> (WORD_SIZE - partialShift);
            result.words[i] <<= partialShift;
            result.words[i] |= carry;
            carry = newCarry;
        }
        result.words[result.words.length - 1] &= (1L << result.remainder) - 1;

        return result;
    }

    public Bigboard right(int amount) {
        Bigboard result = new Bigboard(this);

        int wordShifts = Math.min(amount >> BITS_PER_WORD, result.words.length);
        for (int i = 0; i < result.words.length - wordShifts; i++) {
            result.words[i] = result.words[i + wordShifts];
        }
        for (int i = result.words.length - 1; i >= result.words.length - wordShifts; i--) {
            result.words[i] = 0;
        }

        int partialShift = amount & WORD_SIZE_MASK;
        long carry = 0;
        for (int i = result.words.length - 1; i >= 0; i--) {
            long newCarry = result.words[i] << (WORD_SIZE - partialShift);
            result.words[i] >>>= partialShift;
            result.words[i] |= carry;
            carry = newCarry;
        }

        return result;
    }

    public Bigboard and(long other) {
        Bigboard result = new Bigboard(this);
        result.words[0] &= other;
        return result;
    }

    public Bigboard or(long other) {
        Bigboard result = new Bigboard(this);
        result.words[0] |= other;
        return result;
    }

    public Bigboard xor(long other) {
        Bigboard result = new Bigboard(this);
        result.words[0] ^= other;
        return result;
    }

    /**
     * Returns the bit at the given index on the board. Bits are indexed
     * according to a Little-Endian Rank-File mapping.
     *
     * @param index The index of the position on the board to get the bit of.
     * @return
     */
    public boolean get(int index) {
        return (this.words[index >> BITS_PER_WORD] & (1L << index)) != 0;
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
