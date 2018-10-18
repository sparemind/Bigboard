# Bigboard #
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

Benchmarks can be obtained by running [BigboardPerformanceTest.main](src/BigboardPerformanceTest.java).

* CPU: 2.7 GHz Intel Core i5
* Results are in nanoseconds

| Board Size | AND     | OR      | XOR     | LEFT SHIFT | RIGHT SHIFT | Average | Avg. ops/ms  |
| ---------- | ------- | ------- | ------- | ---------- | ----------- | ------- | ------------ |
| 3x3        | 0.33661 | 0.34589 | 0.39864 | 0.36727    | 0.35593     | 0.36087 | 27,710.90263 |
| 8x8        | 0.31406 | 0.29907 | 0.32768 | 0.35140    | 0.44192     | 0.41900 | 23,866.37746 |
| 13x13      | 0.35712 | 0.32809 | 0.32917 | 0.38563    | 0.36889     | 0.43758 | 22,852.94953 |
| 20x20      | 0.36051 | 0.30770 | 0.31874 | 0.39296    | 0.40691     | 0.44488 | 22,478.06078 |
| 100x100    | 2.78577 | 2.20873 | 2.54127 | 2.81173    | 2.90254     | 2.73899 | 3,650.98667  |

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