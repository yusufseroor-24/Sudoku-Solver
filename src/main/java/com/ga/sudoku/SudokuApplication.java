package com.ga.sudoku;

import com.ga.sudoku.exception.InvalidCharacterException;
import com.ga.sudoku.exception.SudokuFileNotFoundException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootApplication
public class SudokuApplication implements CommandLineRunner {

	private static final Path PUZZLE_FOLDER = Path.of("puzzles");

	public static void main(String[] args) {
		SpringApplication.run(SudokuApplication.class, args);
	}

	@Override
	public void run(String... args){
		for(int i=1; i <= 4; i++){
			solveFile(PUZZLE_FOLDER.resolve("puzzle" + i + ".txt"),"Puzzle " + i);
		}
	}

	private void solveFile(Path file, String title){
		try{
			//read puzzle file
			SudokuBoard sudokuBoard = SudokuBoard.fromFile(file);
			sudokuBoard.printBoard(title);

			//time the solver
			long start = System.nanoTime();
			boolean solved = new SudokuSolver(sudokuBoard).solve();
			long end = System.nanoTime();
			double ms = (end - start) / 1_000_000.0;

			//print it then save it
			if(solved){
				System.out.printf("\t\t%s solved in %.3f ms%n", title, ms);
				sudokuBoard.printBoard(title);
				Path saved = sudokuBoard.saveSudokuSolution(PUZZLE_FOLDER);
				System.out.println(title + " saved in " + saved);
				System.out.println();
			} else {
				System.out.println("No solution to " + title);
			}

		}catch(SudokuFileNotFoundException | InvalidCharacterException e){
			System.out.println("Error: " + e.getMessage() + "\n");
		}catch(IOException e){
			System.out.println("Could not read/write file: " + e.getMessage() + "\n");
		}
	}
}
