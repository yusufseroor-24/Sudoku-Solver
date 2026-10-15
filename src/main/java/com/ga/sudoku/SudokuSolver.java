package com.ga.sudoku;

/**
 * Bitmask is used to instantly know which digits each cell can use
 * Minimum Remaining Values (MRV) chooses the empty cells with the fewest options first
 * backtracking guesses the digit and continues, when stuck, backtrack (undo) the guess and try the next digit

 * Logical idea:
 * Stage 1: Read the file into a normal grid
 * Stage 2: Build the “notes”: It goes through every clue on the board once and ticks it in its row’s list, its column’s list and its box’s list (row 0, column 0, box 0)
 * Stage 3: combines the step 2 checklist and look at empty cell and ask “what can go here”? With bits, “combine and find what’s left” step is one line (~(row | col | box))
 * Stage 4: picks the cell with the least options, fills it and adds it into the checklist
 * */

public class SudokuSolver {

    private static final int SIZE = SudokuBoard.SIZE_OF_BOARD;

    //"0b" Writes the number in bits
    private static final int ALL_DIGITS = 0b1111111110;

    //Sudoku board to be solved
    private final SudokuBoard sudokuBoard;

    //bitmask of the digits
    private final int[] rows = new int[SIZE];
    private final int[] cols = new int[SIZE];
    private final int[] boxes = new int[SIZE];

    public SudokuSolver(SudokuBoard sudokuBoard) {
        this.sudokuBoard = sudokuBoard;
    }

    public boolean solve(){
        //record the clues
        for(int r=0; r < SIZE; r++){
            for (int c=0; c < SIZE; c++){
                int n = sudokuBoard.getCell(r, c).getNum();
                if(n == 0 ) continue;

                //bitmask for the digit, move to the left n numbers
                int bit = 1 << n;

                //check which box is it in?
                int b = boxIndex(r, c);

                //checking if the clues have a duplicated digit (file issue) | & compares the bits in two numbers
                if((rows[r] & bit) != 0  || (cols[c] & bit) != 0 || (boxes[b] & bit) != 0){
                    return false;
                }

                //mark as used in row, column, and box
                rows[r] |= bit;
                cols[c] |= bit;
                boxes[b] |= bit;
            }
        }
        return backtrack();
    }

    //each call fills an empty cell
    private boolean backtrack(){
        int bestEmptyCellRow = -1;
        int bestEmptyCellCol = -1;
        int bestCandidates = 0;
        int bestPossibleCount = 10;

        for(int r=0; r < SIZE; r++ ){
            for(int c=0; c < SIZE; c++){
                //if it's not empty, skip
                if(!sudokuBoard.getCell(r, c).isEmpty()) continue;

                //bitmask of allowed digits
                int candidates = candidates(r, c);
                //counts the 1s
                int count = Integer.bitCount(candidates);

                //MRV
                if(count < bestPossibleCount){
                    bestPossibleCount = count;
                    bestEmptyCellRow = r;
                    bestEmptyCellCol = c;
                    bestCandidates = candidates;
                    if(count == 0) return false; //no possible digits, need to backtrack so return false
                    if(count == 1) break; //it won't find a fewer count so break
                }
            }
            if(bestPossibleCount == 1) break;
        }
        //no empty cell found, which means the board is solved
        if(bestEmptyCellRow == -1){
            return true;
        }

        //try allowed digit in the cell
        SudokuOneCell cell = sudokuBoard.getCell(bestEmptyCellRow, bestEmptyCellCol);
        int b = boxIndex(bestEmptyCellRow, bestEmptyCellCol);

        for(int d=1; d <= 9; d++){
            int bit = 1 << d;
            if((bestCandidates & bit) == 0) continue; //checks if the numbers in the bestCandidates are allowed

            //make a guess and record it
            cell.setNum(d);
            rows[bestEmptyCellRow] |= bit;
            cols[bestEmptyCellCol] |= bit;
            boxes[b] |=bit;

            //do it recursively to solve the whole puzzle
            if(backtrack()){
                return true;
            }

            //dead end, backtrack it (undo)
            cell.setNum(0);
            rows[bestEmptyCellRow] &= ~bit;
            cols[bestEmptyCellCol] &= ~bit;
            boxes[b] &= ~bit;
        }
        return false;
    }

    //which number is allowed to go into the empty cell
    private int candidates(int r, int c){
        int used = rows[r] | cols[c] | boxes[boxIndex(r, c)];
        return ~used & ALL_DIGITS;
    }

    //finds the box number
    private static int boxIndex(int r, int c){
        return (r / 3) * 3 + (c / 3);
    }
}
