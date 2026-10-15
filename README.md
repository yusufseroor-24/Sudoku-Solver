Sudoku Solver 
- 
A Spring Boot application that reads Sudoku puzzles from text files, solves them using backtracking with bitmask and MRV (Minimum Remaining Values). A browser frontend lets you watch the algorithm solve a puzzle step by step or play the puzzle yourself.

Puzzle File Format
- 
Nine rows of nine digits, where `0` (or `.`) is an empty cell. 
Spaces, `|` dividers and `-----` lines are ignored, so both of these work:

```
000150070
106000820
...
```

```
0  0  0  |  1  5  0  |  0  7  0
1  0  6  |  0  0  0  |  8  2  0
----------------------------------
...
```

---

Bitmasking in a nutshell 
- 
To place a digit, the solver must know whether it is already used in that cell's **row**, **column** and **3x3 box**. Scanning those 27 squares every time is slow, so each row, column and box keeps a **checklist** of used digits stored in a single `int`.

Each bit in the integer is one checkbox: bit *d* is `1` if digit *d* is used. For example, a row containing 4, 5, 6, 7 and 9:

```
digit:   1  2  3  4  5  6  7  8  9
row 3:   0  0  0  1  1  1  1  0  1
```

The solver keeps 27 of these integers (9 rows, 9 columns, 9 boxes) and uses four bitwise operations:

| Goal | Code | Meaning |
|---|---|---|
| A checklist with only digit *d* | `1 << d` | shift a 1 into position *d* |
| Is *d* used? | `(mask & (1 << d)) != 0` | AND keeps only the bits on in both |
| Mark *d* as used | `mask \|= (1 << d)` | OR switches that bit on |
| Unmark *d* | `mask &= ~(1 << d)` | AND with "everything except *d*" switches it off |

The digits a cell can still take are found in **one line**:

```java
int used = rows[r] | cols[c] | boxes[box];   
int free = ~used & ALL_DIGITS;              
```

Example:
```
row 3          0  0  0  1  1  1  1  0  1
column 1       1  0  1  1  1  0  0  0  0
box 3          0  1  1  1  0  0  1  0  1
OR (used)      1  1  1  1  1  1  1  0  1
~used (free)   0  0  0  0  0  0  0  1  0 [only 8 can go here]
```
`Integer.bitCount(free)` counts the 1s, which gives how many options the cell has.


How the solver works
- 
1. **Read** the puzzle file into a 9x9 grid.
2. **Build the bitmasks.** Every clue is marked in its row, column and box. If a digit is already marked, the puzzle has a duplicate and is invalid.
3. **Pick a cell (MRV).** Scan all empty cells and choose the one with the **fewest** options. A cell with 1 option is a forced move; a cell with 0 options means an earlier guess was wrong.
4. **Place a digit.** Write it on the board and mark it in the three bitmasks, then repeat from step 3 (recursion).
5. **Backtrack.** If a choice leads to a dead end, erase the digit, unmark its bits, and try the cell's next option. If none work, return `false` so the previous step undoes its choice.
6. **Done.** When no empty cells remain, the board is solved and saved.

Because MRV always fills the most limited cell first, easy puzzles are solved with no guessing at all. Puzzle 1 takes 45 steps for 45 empty cells.

Backend Key Features
- 
- **File reading with validation:** checks that the file exists, contains only valid characters, and has exactly 9 rows of 9 digits.
- **Exceptions:** `SudokuFileNotFoundException` and `InvalidCharacterException` give clear error messages.
- **Set clue protection:** stops any attempt to change a clue from the original puzzle.
- **Fast solver operation:** Bitmasks make each validity check one operation, and MRV keeps the number of guesses very low.
- **Duplicate detection:** Invalid puzzles (the same digit twice in a row, column or box).
- **Saving solutions:** solutions are saved to `puzzles/<name>.solution.txt`.
- **Solve timing:** `System.nanoTime()` is recorded before and after `solve()`, and the console prints how long each puzzle took to be solved.
- **REST API:**
  
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/sudoku/solve/{name}` | Solves `puzzles/{name}.txt`, e.g. `/api/sudoku/solve/puzzle1` |
| `POST` | `/api/sudoku/upload` | Uploads a `.txt` file (form-data key `file`), saves it in `puzzles/`, then solves it |

Frontend Key Features
- 
#### Puzzle loading: 
4 puzzles are provided, additionally, upload puzzle option is provided which sends the file to `/api/sudoku/upload`. The backend's solution is used to check the player's answer.

#### Mode 1 - Watch the algorithm:
- Animates every step: **place**, **guess**, **undo** and **dead end**.
- **Play / Pause**, **Step**, **Reset**, **Skip to end**, and a **speed slider**.
- For each step, shows the cell's options and the **bit table**: row, column and box masks, `OR = used` and `~used = free`.
- Live counters for steps and undos.
- **Solve time stopwatch**
  
> The backend only returns the final board, so the frontend runs a JavaScript copy of the same algorithm (same bitmasks, MRV and backtracking) to record and replay the steps.

#### Mode 2 - Play:
- Click a cell and type 1–9, or use the on-screen number pad. Backspace or 0 erases, and arrow keys move.
- Only digits 1–9 are accepted, and clue cells can't be edited.
- A **timer** tracks how long the player takes and stops when the board matches the solution.
- **Check** shows how many cells are left.

Resources
- 
- https://www.geeksforgeeks.org/dsa/sudoku-backtracking-7/
- https://cahinfor.com/en/blog/sudoku-solver-naive-vs-optimized
- https://www.geeksforgeeks.org/dsa/what-is-bitmasking/
- https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Expressions_and_operators#bitwise_operators
