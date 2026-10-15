package com.ga.sudoku.controller;

import com.ga.sudoku.SudokuBoard;
import com.ga.sudoku.SudokuSolver;
import com.ga.sudoku.exception.InvalidCharacterException;
import com.ga.sudoku.exception.SudokuFileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("api/sudoku")
public class SudokuController {

    private static final Path PUZZLE_FOLDER = Path.of("puzzles");

    @PostMapping("/solve/{name}")
    public ResponseEntity<String> solve(@PathVariable String name) {
        return solvePuzzle(PUZZLE_FOLDER.resolve(name + ".txt"));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAndSolve(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The uploaded file is empty.");
        }

        //checking the file extension
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.endsWith(".txt")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a .txt file.");
        }

        //create a file name from the path (safe file name)
        String fileName = Path.of(originalName).getFileName().toString();
        //creates a destination path (puzzles/{filename})
        Path target = PUZZLE_FOLDER.resolve(fileName).toAbsolutePath();

        try {
            //create puzzles/ folder if it doesn't exist
            Files.createDirectories(PUZZLE_FOLDER);
            //open the uploaded data, and write it into the folder puzzles
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save the uploaded file: " + e.getMessage());
        }
        return solvePuzzle(target);
    }

    private ResponseEntity<String> solvePuzzle(Path file) {
        try {
            //read
            SudokuBoard sudokuBoard = SudokuBoard.fromFile(file);
            //solve
            if (!new SudokuSolver(sudokuBoard).solve()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No solution to " + sudokuBoard.getName());
            }
            //save
            Path saved = sudokuBoard.saveSudokuSolution(PUZZLE_FOLDER);
            return ResponseEntity.ok("Saved to " + saved + System.lineSeparator()
                    + System.lineSeparator() + sudokuBoard.boardToString());
        } catch (SudokuFileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidCharacterException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not read/write file: " + e.getMessage());
        }
    }
}
