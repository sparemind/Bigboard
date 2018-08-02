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
 * <p>
 * This class represents an immutable bit array data structure where each bit
 * corresponds to a position on a 2D board.
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
    private final int width;
    private final int height;
    private final int size;

    public Bigboard(int width, int height) {
        this.width = width;
        this.height = height;
        this.size = width * height;
        this.words = new long[((this.size - 1) >> BITS_PER_WORD) + 1];
    }

    public Bigboard(Bigboard other) {
        this.width = other.width;
        this.height = other.height;
        this.size = other.size;
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

    public boolean get(int index) {
        return (this.words[index >> BITS_PER_WORD] & (1L << index)) != 0;
    }

    /**
     * LERF
     *
     * @return
     */
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
}
