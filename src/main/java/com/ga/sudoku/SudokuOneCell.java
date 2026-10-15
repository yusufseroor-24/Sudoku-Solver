package com.ga.sudoku;

//represents one square cell in the sudoku board
public class SudokuOneCell{

    private final int row;
    private final int col;
    private int num;
    private final boolean fixedClue;

    public SudokuOneCell(int row, int col, int num) {
        this.row = row;
        this.col = col;
        this.num = num;
        //any number in the file is a clue if it's not a zero
        this.fixedClue = num !=0;
    }

    public int getRow() {
        return row;
    }

    public boolean isFixedClue() {
        return fixedClue;
    }

    public boolean isEmpty(){
        return num == 0;
    }

    public int getNum() {
        return num;
    }

    public int getCol() {
        return col;
    }


    //which 3X3 box
    public int getSquareBox(){
        return (row / 3) * 3 + (col / 3);
    }

    //changes the number, and if it tries to change a fixed clue number (set from the puzzle) it shows an error
    public void setNum(int num){
        if(fixedClue){
            throw new RuntimeException("The number you are trying to change is a clue and can't be changed");
        }
        if(num < 0 || num > 9){
            throw new RuntimeException("Value must be 0 - 9 only");
        }
        this.num = num;
    }

    @Override
    public String toString() {
        return String.valueOf(num);
    }
}
