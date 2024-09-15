package ca.jrvs.apps.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class JavaGrepImp implements JavaGrep{
    protected static final Logger logger = LoggerFactory.getLogger(JavaGrepImp.class);
    private String regex;
    private String rootPath;
    private String outFile;



    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("INVALID NUMBER OF ARGUMENTS, expected 3 arguments, got " + args.length);
        }

        JavaGrepImp javaGrepImp = new JavaGrepImp();
        javaGrepImp.setRegex(args[0]);
        javaGrepImp.setRootPath(args[1]);
        javaGrepImp.setOutFile(args[2]);
        try {
            javaGrepImp.process();
        } catch (Exception EXCEPT) {
            logger.error("an error has occurred", EXCEPT);

        }
    }



    public void process() throws IOException {
        List<String> matchedLines = new ArrayList<>();

        // Iterate through all files in the root directory
        for (File file : listFiles(rootPath)) {
            // Iterate through each line in the file
            for (String line : readLines(file)) {
                // Check if the line matches the pattern
                if (containsPattern(line)) {
                    matchedLines.add(line);
                    // Add matching lines to the list
                }
            }
        }

        // Write all matched lines to the output file
        writeToFile(matchedLines);
    }


    public List<File> listFiles(String rootDir) {
        File rootFile = new File(rootDir);
        List<File> allFiles = new ArrayList<>();

        // Check if root directory exists, return empty list if not
        if (!rootFile.exists()) {
            logger.warn("Directory does not exist: {}", rootDir);
            return Collections.emptyList();
        }

        // If the root is a file, return a list with just that file
        if (!rootFile.isDirectory()) {
            allFiles.add(rootFile);
            return allFiles;
        }

        // Initialize a deque to store directories to explore
        Deque<File> dirsToExplore = new ArrayDeque<>();
        dirsToExplore.add(rootFile);

        // Traverse directories iteratively
        while (!dirsToExplore.isEmpty()) {
            File currentDir = dirsToExplore.pop();
            File[] filesInDir = currentDir.listFiles();

            // If directory has contents, iterate through them
            if (filesInDir != null) {
                for (File file : filesInDir) {
                    // If it's a directory, add to the deque to explore later
                    if (file.isDirectory()) {
                        dirsToExplore.push(file);
                    } else {
                        // If it's a file, add to the list of files
                        allFiles.add(file);
                    }
                }
            }
        }

        // Return the list of all files found
        return allFiles;
    }



    public List<String> readLines(File inputFile) {
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File does not exist: " + inputFile.getAbsolutePath());
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader R = new BufferedReader(new FileReader(inputFile))) {
            String currentLine;
            while ((currentLine = R.readLine()) != null) {
                lines.add(currentLine);
            }
        } catch (IOException e) {
            logger.error("Could not read file: {}", inputFile.getName(), e);
        }

        return lines;
    }


    public boolean containsPattern(String line) {
        return Pattern.matches(getRegex(), line);
    }

    public void writeToFile(List<String> lines) throws IOException {
        File OFile = new File(outFile);

        try (BufferedWriter W = new BufferedWriter(new FileWriter(OFile))) {
            for (String line : lines) {
                W.write(line);
                W.newLine();
            }
        } catch (IOException e) {
            logger.error("Could not write to output file: {}", OFile.getName(), e);
        }
    }


    public String getRootPath() {
        return rootPath;
    }


    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;

    }


    public String getRegex() {
        return regex;
    }


    public void setRegex(String regex) {
        this.regex = regex;

    }


    public String getOutFile() {
        return outFile;
    }


    public void setOutFile(String outFile) {
        this.outFile = outFile;

    }
}


