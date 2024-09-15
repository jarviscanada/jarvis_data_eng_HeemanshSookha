package ca.jrvs.apps.grep;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface JavaGrep {

    /**
     * Implements the JavaGrep Application. Top level search workflow that recursively searches
     * through directories for text that matches a RegEx pattern.
     *
     * @throws IOException If a given file could not be read.
     */
    void process() throws IOException;

    /**
     * Recursively traverses a given directory and returns all files. If {@code rootDir} is a file,
     * returns a {@link List} only containing the {@link File} at the path specified by
     * {@code rootDir}.
     *
     * @param rootDir File path of input directory or file.
     * @return A {@link List} of files with all files under {@code rootDir}.
     */
    List<File> listFiles(String rootDir);

    /**
     * Reads a file and return all of its lines.
     *
     * @param inputFile {@link File} to be read
     * @return {@link List} of lines in the file.
     * @throws IllegalArgumentException If a given input file does not exist.
     */
    List<String> readLines(File inputFile) throws IllegalArgumentException;

    /**
     * Checks if a line contains the regex pattern (passed by user).
     *
     * @param line input string.
     * @return true if there is a match.
     */
    boolean containsPattern(String line);

    /**
     * Write lines to a file. Creates a {@code BufferedWriter} to write lines one at a time to output
     * file (specified by user).
     * <p>
     * Output file is overwritten if it already exists
     *
     * @param lines
     * @throws IOException If writing fails.
     */
    void writeToFile(List<String> lines) throws IOException;

    String getRootPath();

    /**
     * Sets root directory or file form which the search is executed.
     *
     * @param rootPath
     */
    void setRootPath(String rootPath);

    String getRegex();

    /**
     * Sets the RegEx pattern used to match lines against.
     *
     * @param regex
     */
    void setRegex(String regex);

    String getOutFile();

    /**
     * Sets the location of the output file where lines matching the regular expression pattern set by
     * {@link #setRegex(String)} will be written.
     *
     * @param outFile
     */
    void setOutFile(String outFile);
}