
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {"/InsertBookFlight"})
public class InsertBookFlight extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        Gson gson = new Gson();

        PrintWriter out = response.getWriter();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }

            JsonData data = gson.fromJson(jsonBuffer.toString(), JsonData.class);

            String db_url = "jdbc:mysql://localhost:3306/airline_db";
            String db_userName = "root";
            String db_password = "user";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(db_url, db_userName, db_password);

            String paymentQuery = "INSERT INTO payments (userId, cardholdersName, cardNumber, cost) VALUES (?, ?, ?, ?)";
            PreparedStatement paymentStatement = connection.prepareStatement(paymentQuery,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            paymentStatement.setString(1, data.getUserId());
            paymentStatement.setString(2, data.getCardholdersName());
            paymentStatement.setString(3, data.getCardNumber());
            paymentStatement.setInt(4, data.getTotal());
            int affectedRows = paymentStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting payment failed, no rows affected.");
            }

            // Retrieve generated payment ID
            int generatedPaymentId;
            try (ResultSet generatedKeys = paymentStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedPaymentId = generatedKeys.getInt(1);
                    System.out.println("Inserted Payment ID: " + generatedPaymentId);
                } else {
                    throw new SQLException("Inserting payment failed, no ID obtained.");
                }
            }

            // Insert booked flights for business seats
            List<Seat> businessSeats = data.getBusinessSeats();
            for (Seat seat : businessSeats) {
                String insertFlightQuery = "INSERT INTO BookFlights (userId, flightId, flightClassType, rowNumber, colNumber, paymentId) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertFlightStatement = connection.prepareStatement(insertFlightQuery);
                insertFlightStatement.setString(1, data.getUserId());
                insertFlightStatement.setString(2, data.getFlightId());
                insertFlightStatement.setString(3, "business");
                insertFlightStatement.setInt(4, seat.getRow());
                insertFlightStatement.setInt(5, seat.getCol());
                insertFlightStatement.setInt(6, generatedPaymentId);
                int result = insertFlightStatement.executeUpdate();
                if (result != 0) {
                    System.out.println("Inserted flight for seat: Row " + seat.getRow() + ", Col " + seat.getCol());
                } else {
                    System.err.println("Failed to insert flight for seat: Row " + seat.getRow() + ", Col " + seat.getCol());
                }
            }
            List<Seat> economySeats = data.getEconomySeats();
            for (Seat seat : economySeats) {
                String insertFlightQuery = "INSERT INTO BookFlights (userId, flightId, flightClassType, rowNumber, colNumber, paymentId) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertFlightStatement = connection.prepareStatement(insertFlightQuery);
                insertFlightStatement.setString(1, data.getUserId());
                insertFlightStatement.setString(2, data.getFlightId());
                insertFlightStatement.setString(3, "economy");
                insertFlightStatement.setInt(4, seat.getRow());
                insertFlightStatement.setInt(5, seat.getCol());
                insertFlightStatement.setInt(6, generatedPaymentId);
                int result = insertFlightStatement.executeUpdate();
                if (result != 0) {
                    System.out.println("Inserted flight for seat: Row " + seat.getRow() + ", Col " + seat.getCol());
                } else {
                    System.err.println("Failed to insert flight for seat: Row " + seat.getRow() + ", Col " + seat.getCol());
                }
            }
            SendData send = new SendData();
            send.setStatus("ok");
            String responseJson = gson.toJson(send);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write(responseJson);

        } catch (Exception ex) {
            SendData errorResponse = new SendData();
            errorResponse.setStatus("error: " + ex.getMessage());
            String errorJson = gson.toJson(errorResponse);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write(errorJson);
            ex.printStackTrace(); // Log the exception for debugging
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Handle or log this exception
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Insert Book Flight Servlet";
    }

    public static class JsonData {

        private String flightId;
        private String cardholdersName;
        private String cardNumber;
        private String userId;
        private List<Seat> businesSeats;
        private List<Seat> economySeats;
        private int total;

        public String getFlightId() {
            return flightId;
        }

        public void setFlightId(String flightId) {
            this.flightId = flightId;
        }

        public String getCardholdersName() {
            return cardholdersName;
        }

        public void setCardholdersName(String cardholdersName) {
            this.cardholdersName = cardholdersName;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<Seat> getBusinessSeats() {
            return businesSeats;
        }

        public void setBusinessSeats(List<Seat> businessSeats) {
            this.businesSeats = businessSeats;
        }

        public List<Seat> getEconomySeats() {
            return economySeats;
        }

        public void setEconomySeats(List<Seat> economySeats) {
            this.economySeats = economySeats;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }

    public static class Seat {

        private int row;
        private int col;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }
    }

    public static class SendData {

        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
