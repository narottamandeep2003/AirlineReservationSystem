import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/GetBookedFlightSeats"})
public class GetBookedFlightSeats extends HttpServlet {

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

            Flight data = gson.fromJson(jsonBuffer.toString(), Flight.class);

            String db_url = "jdbc:mysql://localhost:3306/airline_db";
            String db_userName = "root";
            String db_password = "user";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(db_url, db_userName, db_password);

            HashSet<Seat> res = new HashSet<>();
            statement = connection.prepareStatement("SELECT * FROM bookflights WHERE flightId=?");
            statement.setString(1, data.flightId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                res.add(new Seat(resultSet.getInt("rowNumber"),
                        resultSet.getInt("colNumber"), resultSet.getString("flightClassType")));
            }

            SendData send = new SendData();
            send.status = "ok";
            send.flight = res;
            String responseJson = gson.toJson(send);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write(responseJson);

        } catch (Exception ex) {
            SendData errorResponse = new SendData();
            errorResponse.status = "error " + ex.getMessage() + "#1";
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

    class Flight {
        String flightId;
    }
    
    class Seat {
        int rowNumber;
        int colNumber;
        String flightClassType;
        
        public Seat(int rowNumber, int colNumber, String flightClassType) {
            this.rowNumber = rowNumber;
            this.colNumber = colNumber;
            this.flightClassType = flightClassType;
        }
    }
    
    class SendData {
        String status;
        HashSet<Seat> flight;
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
        return "Get Booked Flight Seats Servlet";
    }

}
