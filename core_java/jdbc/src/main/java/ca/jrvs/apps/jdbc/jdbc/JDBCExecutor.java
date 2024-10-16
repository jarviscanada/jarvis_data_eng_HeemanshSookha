package ca.jrvs.apps.jdbc.jdbc;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class JDBCExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JDBCExecutor.class);
    public static void main(String... args){
        DatabaseConnectionManager dcm = new DatabaseConnectionManager(
                "localhost:5432",
                "hplussport",
                "postgres",
                "password");
        try{
            Connection connection = dcm.getConnection();
            CustomerDAO customerDAO = new CustomerDAO(connection);
            Customer customer = new Customer();
            customer.setFirstName("Sookha");
            customer.setLastName("Heemansh");
            customer.setEmail("jdjbne@gmail.com");
            customer.setPhone("(123) 345-6789");
            customer.setAddress("summerhill");
            customer.setCity("montreal");
            customer.setState("QC");
            customer.setZipCode("H3H 1C5");
            customerDAO.create(customer);
        }catch(SQLException e){
            logger.error("SQL Exception occurred", e);
        }
    }
}
