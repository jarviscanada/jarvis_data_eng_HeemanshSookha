package ca.jrvs.apps.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.BasicConfigurator;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaGrepLambdaImp implements JavaGrep {
    protected static final Logger logger = LoggerFactory.getLogger(JavaGrepLambdaImp.class);
    private String regex;
    private String rootPath;
    private String outFile;

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("INVALID NUMBER OF ARGUMENTS, expected 3 arguments, got " + args.length);
        }


        BasicConfigurator.configure();
        JavaGrepLambdaImp javaGrepLambdaImp = new JavaGrepLambdaImp();
        javaGrepLambdaImp.setRegex(args[0]);
        javaGrepLambdaImp.setRootPath(args[1]);
        javaGrepLambdaImp.setOutFile(args[2]);

        try {
            javaGrepLambdaImp.process();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
        }
    }

    public void process() throws IOException {
        // Filter files and lines using lambdas
        List<String> matchedLines = listFiles(rootPath).stream()
                .flatMap(file -> {
                    return readLines(file).stream();
                })
                .filter(this::containsPattern)
                .collect(Collectors.toList());

        // Write the matched lines to the output file
        writeToFile(matchedLines);
    }

    public List<File> listFiles(String rootDir) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(rootDir))) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Could not list files in directory: {}", rootDir, e);
            return Collections.emptyList();
        }
    }

    public List<String> readLines(File inputFile) throws IllegalArgumentException{
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File does not exist: " + inputFile.getAbsolutePath());
        }

        try (Stream<String> lines = Files.lines(inputFile.toPath())) {
            return lines.collect(Collectors.toList());
        }catch (IOException ex) {
            logger.warn("Skipping file (could not read) " + inputFile.getPath());
            return new ArrayList<>();
        }

    }

    public boolean containsPattern(String line) {
        return Pattern.matches(getRegex(), line);
    }

    public void writeToFile(List<String> lines) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Could not write to output file: {}", outFile, e);
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
