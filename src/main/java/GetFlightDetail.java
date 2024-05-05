import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet(urlPatterns = {"/GetFlightDetail"})
public class GetFlightDetail extends HttpServlet {

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

            FlightData data = gson.fromJson(jsonBuffer.toString(), FlightData.class);

            String db_url = "jdbc:mysql://localhost:3306/airline_db";
            String db_userName = "root";
            String db_password = "user";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(db_url, db_userName, db_password);

            statement = connection.prepareStatement("SELECT * FROM \n" +
"(SELECT flightId, airline,airplanes.userid, airplanes.airplaneId, Departuredate, departureFrom, destination, Departuretime,\n" +
"EconomyPrice,BusinessPrice,EconomyRow,EconomyColumn,BusinessRow,BusinessColumn \n" +
"FROM flights INNER JOIN airplanes ON airplanes.airplaneId = flights.airplaneId AND  flights.flightId = ? ) as f;");
            statement.setString(1, data.flightId);
            resultSet = statement.executeQuery();

            Flight f = null;
            while (resultSet.next()) {
                f = new Flight(resultSet.getString("flightId"), resultSet.getString("airline"),
                        resultSet.getString("userid"), resultSet.getString("Departuredate"),
                        resultSet.getString("Departuretime"),
                        resultSet.getString("departureFrom"), resultSet.getString("destination"),
                        resultSet.getInt("BusinessPrice"), resultSet.getInt("EconomyPrice"),
                        resultSet.getInt("BusinessRow"), resultSet.getInt("BusinessColumn"),
                        resultSet.getInt("EconomyRow"), resultSet.getInt("EconomyColumn"));
            }

            
        

            SendData send = new SendData();
            send.status = "ok";
            send.flight = f;
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

    class FlightData {

        String flightId;
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
        return "Get Flight Detail Servlet";
    }

    class Flight {

        String flightId;
        String airline;
        String userid;
        String Departuredate;
        String departureFrom;
        String destination;
        String departuretime;
        int EconomyPrice;
        int BusinessPrice;
        int EconomyRow;
        int EconomyColumn;
        int BusinessRow;
        int BusinessColumn;

        public Flight(String flightId, String airline, String userid, String Departuredate,String departuretime , String destination, String departureFrom, int BusinessPrice, int EconomyPrice, int BusinessRow, int BusinessColumn, int EconomyRow, int EconomyColumn) {
            this.flightId = flightId;
            this.airline = airline;
            this.userid = userid;
            this.Departuredate = Departuredate;
            this.departureFrom = departureFrom;
            this.destination = destination;
            this.departuretime = departuretime;
            this.EconomyPrice = EconomyPrice;
            this.BusinessPrice = BusinessPrice;
            this.EconomyRow = EconomyRow;
            this.EconomyColumn = EconomyColumn;
            this.BusinessRow = BusinessRow;
            this.BusinessColumn = BusinessColumn;
        }
    }

    class SendData {

        String status;
        Flight flight;

        public SendData() {
        }
    }
}
