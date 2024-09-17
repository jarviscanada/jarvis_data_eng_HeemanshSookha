
# Introduction
This project is a Java application that searches files for lines matching a given regex pattern. It processes files in a specified directory line by line and outputs matched lines to a file. The application mirrors GNU's grep functionality, leveraging Java for cross-platform compatibility via the JVM.

## Technologies
- **Java:** Implements the grep functionality, handling IO operations and pattern matching.
- **Maven:** Manages project dependencies and compiles the application.
- **Docker:** Containers the application to ensure isolated, consistent runtime environments.
- **Git:** Tracks version control and project progress.

# Quick Start
To start using the application
1. Clone the repository
```
git clone https://github.com/jarviscanada/jarvis_data_eng_HeemanshSookha.git
```
2. Package the Java Grep application
```
mvn clean package
```
3. Login to Docker Hub
```
docker_user=<docker-id>
docker login -u ${docker_user} --password-stdin
```
4. Build a new Docker image
```
docker build -t ${docker_user}/grep .
docker image ls | grep "grep" # To verify if image exists
```
5. Run Docker image
```
docker run --rm \
-v `pwd`/data:/data -v `pwd/log:/log \
${docker_user}/grep <pattern> <input-directory> <output-file-path>
```

# Implementation
## Pseudocode
The program takes three inputs: `pattern`, `rootDir`, and `outputFilePath`. It scans each file under `rootDir`, checking each line for matches with `pattern`. The results are saved in `outputFilePath`.
```
matchedLines = []
for file in listFilesRecursively(rootDir)
  for line in readLines(file)
      if containsPattern(line)
        matchedLines.add(line)
writeToFile(matchedLines)
```

## Performance Issue
Currently, the program loads all files into memory before processing, which can exceed JVM heap limits and cause failures. Additionally, it processes files one by one, resulting in suboptimal performance for large datasets.

# Test
As for testing, I used manual testing where i sampled through the data following a Regex and then compared results with the program

# Deployment
The application is packaged using **Maven** and containerized using **Docker**. We compile the entire program and it's related Apache libraries as a `.jar` file using **Maven** and we containerized using **Docker** to allow the user to run the application without any prior dependencies or installations.

# Improvement
- Implement file-by-file processing to reduce memory consumption and avoid heap overflows.
- Introduce asynchronous file reading and pattern matching for greater efficiency in handling large datasets.
- Add logging to track program progress and performance metrics.
- Provide flexible output options, including different formats (e.g., CSV, JSON) to support various user needs.
