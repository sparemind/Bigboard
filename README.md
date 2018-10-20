# Bigboard #
![Travis CI Status](https://travis-ci.org/veylence/Bigboard.svg?branch=master)

A bitboard class generalized to work for boards both larger and smaller
than the traditional 8x8.

## Overview ##
Creating a new Bigboard will create an empty, immutable board.
Performing operations on this board will produce new boards while
leaving the original unchanged.

**Features include:**

* Standard bitwise operation (and, or, shift, etc.)
* Extended bitwise operations (flip, set, etc.)
* Optimized special operation (LSB manipulation, pop count, etc.)
* Thread-safe
* Full JavaDoc documentation

## Benchmarks ##

Benchmarks can be obtained by running [BigboardPerformanceTest.main](src/main/java/BigboardPerformanceTest.java).

* CPU: 2.7 GHz Intel Core i5
* Results are in millisecond per operation, unless otherwise specified

| Board Size | AND      | OR       | XOR      | LEFT SHIFT  | RIGHT SHIFT  | Average  | Avg. ops/ms   |
| ---------- | -------- | -------- | -------- | ----------- | ------------ | -------- | ------------- |
| 3x3        | 0.000033 | 0.000032 | 0.000032 | 0.000043    | 0.000035     | 0.000035 | 28,553.104820 |
| 8x8        | 0.000031 | 0.000032 | 0.000033 | 0.000033    | 0.000040     | 0.000034 | 29,647.438640 |
| 13x13      | 0.000033 | 0.000038 | 0.000033 | 0.000038    | 0.000037     | 0.000036 | 27,905.638939 |
| 20x20      | 0.000037 | 0.000031 | 0.000033 | 0.000038    | 0.000040     | 0.000036 | 27,895.541969 |
| 100x100    | 0.000279 | 0.000222 | 0.000220 | 0.000281    | 0.000289     | 0.000258 | 3,873.410703  |

## Examples ##
The following example demonstrates the implementation of an occluded
fill that could be used to generate a partial set of sliding moves for
some chess position. The first code block is a standard implementation
for an 8x8 board, while the second uses Bigboard to generalize the
function to work for NxM boards.
```java
// From: https://www.chessprogramming.org/Dumb7Fill
public long fillSouth(long gen, long pro) {
    for (int i = 0; i < 7; i++) {
        gen |= pro & (gen >> 8);
    }
    return gen;
}
```
```java
public Bigboard fillSouth(Bigboard gen, Bigboard pro) {
    for (int i = 0; i < gen.width() - 1; i++) {
        gen = gen.or(pro.and(gen.right(gen.width())));
    }
    return gen;
}
```
A demonstration of the function in use on a 13x6 board:
```
Bigboard empty = new Bigboard(13, 6);
Bigboard gen = empty.set(6, 4).set(2, 5).set(11, 2);
Bigboard pro = empty.set(6, 1).not();
Bigboard res = fillSouth(gen, pro);

gen:                           pro:                           result:
. . X . . . . . . . . . .      X X X X X X X X X X X X X      . . X . . . . . . . . . .
. . . . . . X . . . . . .      X X X X X X X X X X X X X      . . X . . . X . . . . . .
. . . . . . . . . . . . .      X X X X X X X X X X X X X      . . X . . . X . . . . . .
. . . . . . . . . . . X .      X X X X X X X X X X X X X      . . X . . . X . . . . X .
. . . . . . . . . . . . .      X X X X X X . X X X X X X      . . X . . . . . . . . X .
. . . . . . . . . . . . .      X X X X X X X X X X X X X      . . X . . . . . . . . X .
```

## License ##
This project is licensed under the MIT license.