
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/InsertFlight"})
public class InsertFlight extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder jsonBuffer;

        try (BufferedReader reader = request.getReader()) {
            jsonBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        Gson gson = new Gson();
        Flight flight = gson.fromJson(jsonBuffer.toString(), Flight.class);

        String dbUrl = "jdbc:mysql://localhost:3306/airline_db";
        String dbUserName = "root";
        String dbPassword = "user";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
                String insertFlightQuery = "INSERT INTO flights (airplaneId, userid, EconomyPrice, BusinessPrice, Departuredate, departureFrom, destination, Departuretime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(insertFlightQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1, flight.airplaneId);
                statement.setString(2, flight.userid);
                statement.setInt(3, flight.economyPrice);
                statement.setInt(4, flight.businessPrice);
                statement.setString(5, flight.departureDate);
                statement.setString(6, flight.departureFrom);
                statement.setString(7, flight.destination);
                statement.setString(8, flight.departureTime);
                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Inserting Plane failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        responseObject res = new responseObject();
                        res.status = "ok";
                        String jsonResponse = gson.toJson(res);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(jsonResponse);
                    } else {
                        responseObject res = new responseObject();
                        res.status = "error7";
                        String jsonResponse = gson.toJson(res);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(jsonResponse);
                    }
                }
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            responseObject res = new responseObject();
            res.status = "error"+e.getMessage();
            String jsonResponse = gson.toJson(res);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);
        }
    }

    static class Flight {

        int airplaneId;
        String userid;
        int economyPrice;
        int businessPrice;
        String departureDate;
        String departureTime;
        String departureFrom;
        String destination;
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
        return "Short description";
    }

    static class responseObject {

        public String status;
    }
}
