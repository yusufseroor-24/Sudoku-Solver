package com.ga.sudoku;

import com.ga.sudoku.exception.InvalidCharacterException;
import com.ga.sudoku.exception.SudokuFileNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * method to reads the puzzles, checks for correct numbers (9) in each row and 9 rows (validation)
 * method to construct the board with boardToString into readable text and sudoku like shape
 * method to save the solution
 * method to print the board
 */

public class SudokuBoard {

    public static final int SIZE_OF_BOARD = 9;

    //puzzle name
    private final String name;
    //9x9 grid board
    private final SudokuOneCell[][] grid = new SudokuOneCell[SIZE_OF_BOARD][SIZE_OF_BOARD];

    private SudokuBoard(String name) {
        this.name = name;
    }

    //Read the file
    public static SudokuBoard fromFile(Path file)
        throws SudokuFileNotFoundException, InvalidCharacterException, IOException{
        if(!Files.exists(file)){
            throw new SudokuFileNotFoundException("Puzzle file is not found " + file.toAbsolutePath());
        }

        String fileName = file.getFileName().toString();
        String name = fileName.endsWith(".txt")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;

        SudokuBoard sudokuBoard = new SudokuBoard(name);

        //array to collect the rows from the file (check if it has 9 valid rows)
        List<int[]> rows = new ArrayList<>();

        //read the file (string per line)
        List<String> lines = Files.readAllLines(file);

        for (int lineNum=0; lineNum < lines.size(); lineNum++){
            String line = lines.get(lineNum);
            if(line.isBlank() || line.strip().matches("-+")){
                continue;
            }

            int[] row = new int[SIZE_OF_BOARD];
            //how many valid nums in the line
            int countOfNums = 0;

            for(int i=0; i < line.length(); i++){
                char charac = line.charAt(i);
                if(charac == ' ' || charac == '\t' || charac == '|'){
                    continue;
                }
                if(charac != '.' && (charac < '0' || charac > '9')){
                    throw new InvalidCharacterException("Invalid character available");
                }
                if(countOfNums == SIZE_OF_BOARD){
                    throw new InvalidCharacterException("Too many numbers in one of the lines (expected number is 9)");
                }

                //stores the character to an integer and stores it in the next available position
                row[countOfNums++] = (charac == '.') ? 0 : charac - '0';
            }
            if (countOfNums != SIZE_OF_BOARD){
                throw new InvalidCharacterException("A line has less than 9 numbers");
            }
            rows.add(row);
        }
        if(rows.size() != SIZE_OF_BOARD){
            throw new InvalidCharacterException("File must have exactly 9 rows");
        }

        //turns numbers to Sudoku cell objects and place it in the correct place
        for(int r=0; r < SIZE_OF_BOARD; r++){
            for(int c=0; c < SIZE_OF_BOARD; c++){
                sudokuBoard.grid[r][c] = new SudokuOneCell(r, c, rows.get(r)[c]);
            }
        }
        return sudokuBoard;
    }

    //Save the solution
    public Path saveSudokuSolution(Path folder) throws IOException{
        Path solutionFile = folder.resolve(name + ".solution.txt");
        Files.writeString(solutionFile, boardToString());
        return solutionFile;
    }

    //print the board
    public void printBoard(String title){
        System.out.println("\t\t" + title);
        System.out.println(boardToString());
        System.out.println();

    }

    //builds the board into readable text
    public String boardToString(){
        StringBuilder builder = new StringBuilder();
        for (int r=0; r < SIZE_OF_BOARD; r++){
            for (int c=0; c < SIZE_OF_BOARD; c++){
                builder.append(grid[r][c].getNum());
                if(c == 2 || c == 5){
                    builder.append("  |  ");
                } else if(c < SIZE_OF_BOARD - 1){
                    builder.append("    ");
                }
            }
            //end of row and new line
            builder.append(System.lineSeparator());
            if(r == 2 || r == 5){
                builder.append("----------------------------------").append(System.lineSeparator());
            }
        }
        return builder.toString();
    }


    //helpers used by the solver to read and change square cells
    public SudokuOneCell getCell(int row, int col){
        return grid[row][col];
    }

    public String getName(){
        return  name;
    }
}
