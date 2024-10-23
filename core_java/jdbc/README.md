
# Stock Quote App

## Introduction

The Stock Quote App is a Java application that simulates managing a stock portfolio by retrieving stock data from the Alpha Vantage API and storing it in a PostgreSQL database. This application allows users to view stock information, purchase stocks, and sell stocks, thereby effectively managing their portfolio. The app is built using technologies such as JDBC for database interaction, PostgreSQL for data storage, Maven for project management, and OkHttp for API communication.

## Implementation



### Design Patterns

DAO Pattern: This pattern is utilized to separate database logic from business logic, providing an abstraction layer for database operations. Specifically, QuoteDao and PositionDao manage interactions with the quote and position tables within the PostgreSQL database. By using this pattern, database operations are distinctly separated from the business logic encapsulated within service classes like QuoteService and PositionService.e.

## Test

The Stock Quote App undergoes rigorous testing with both unit tests and integration tests. JUnit is used to verify that each method functions correctly in isolation (unit testing), while Mockito is employed to mock dependencies when necessary. This strategy allows us to validate individual components like QuoteDao and PositionDao without requiring a live database connection for every test.

For integration testing, we ensure that the entire system works cohesively, including interactions with the PostgreSQL database. These tests use real database connections to confirm that stock transactions (buying, selling, viewing) are executed correctly and data is properly persisted.

### Unit Testing

JUnit tests are used to verify each method within the DAO classes (QuoteDao and PositionDao) without mocking the database connection, ensuring that operations from fetching stock data to saving positions function as expected.
In the service layer (QuoteService, PositionService), Mockito is utilized to mock external dependencies like API calls, confirming that business logic operates correctly even when external services are unavailable.
### Integration Testing

Integration tests are conducted by executing the entire app within a real environment using a PostgreSQL database, testing the app’s functionality end-to-end. This ensures that all components of the app, including data persistence, stock retrieval, and business logic, interact seamlessly.
Console testing is performed by running the application via the Main class, allowing users to buy, sell, and view stocks through the actual console interface. This verifies that the user experience and interactions are intuitive and seamless.

### Database Setup

PostgreSQL serves as the database backend, configured with a quote table for storing stock information and a position table for tracking user positions.
During tests, test data for both quotes and positions is inserted to simulate real trading scenarios, validating that data is saved, retrieved, and deleted correctly.

## Deployment

The application is packaged using Maven, and a Docker container is used to set up the PostgreSQL database for testing and production use.

### Steps to Deploy

1. **Register a Docker Hub account** if you don’t already have one.
2. **Package your Java app** using Maven:
   ```bash
   mvn clean package
	```
Build a Docker image for your app:

```bash
docker build -t your-dockerhub-username/stock-quote-app
```
Verify your image:
```bash
docker images
```
Run your Docker container:
```bash
docker run -p 8080:8080 your-dockerhub-username/stock-quote-app
```
Push your image to Docker Hub:
```bash
docker push your-dockerhub-username/stock-quote-app
```
Verify your image on Docker Hub

# Quick Start

## Option 1: Run the app locally

Clone the repository:
```bash
git clone git@github.com:jarviscanada/jarvis_data_eng_HeemanshSookha.git
```
Navigate to the core_java/jdbc directory:
```bash
cd jarvis_data_eng_HeemanshSookha/core_java/jdbc
```
Ensure you have Docker installed and running. Start a PostgreSQL container with the following command:
```bash
docker run --name stock_quote -e POSTGRES_PASSWORD=password -d -p 5432:5432 postgres
```
Use Maven to build the project:
```bash
mvn clean package
```
Run the application:
```bash
java -jar target/stockquote-app.jar
```




