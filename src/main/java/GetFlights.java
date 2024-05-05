
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(urlPatterns = {"/GetFlights"})
public class GetFlights extends HttpServlet {

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

//                data requestData = gson.fromJson(jsonBuffer.toString(), data.class);

                String db_url = "jdbc:mysql://localhost:3306/airline_db";
                String db_userName = "root";
                String db_password = "user";

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(db_url, db_userName, db_password);

                String query = true
                        ? "SELECT * FROM (SELECT flightId, EconomyPrice, airline, airplanes.userid, airplanes.airplaneId, Departuredate, departureFrom, destination, Departuretime FROM flights LEFT JOIN airplanes ON airplanes.airplaneId = flights.airplaneId ) AS f WHERE (f.departureDate > ? OR (f.departureDate = ? AND f.departureTime > ? ))"
                        : "SELECT * FROM (SELECT flightId, EconomyPrice, airline, airplanes.userid, airplanes.airplaneId, Departuredate, departureFrom, destination, Departuretime FROM flights LEFT JOIN airplanes ON airplanes.airplaneId = flights.airplaneId ) AS f WHERE (f.departureDate > ? OR (f.departureDate = ? AND f.departureTime > ? )) AND (f.departureFrom=? OR f.destination=? OR f.airline=?)";
                statement = connection.prepareStatement(query);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
                String currDate = dateFormat.format(new Date());

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                String currTime = timeFormat.format(new Date());

                statement.setString(1, currDate);
                statement.setString(2, currDate);
                statement.setString(3, currTime);
                if (!true) {
                    statement.setString(4, "");
                    statement.setString(5, "");
                    statement.setString(6, "");
                }

                resultSet = statement.executeQuery();

                ArrayList<Flight> flights = new ArrayList<>();
                while (resultSet.next()) {
                    Flight flight = new Flight(resultSet.getString("flightId"), resultSet.getString("airline"),
                            resultSet.getString("userid"), resultSet.getString("Departuredate"),
                            resultSet.getString("Departuretime"),
                            resultSet.getString("departureFrom"), resultSet.getString("destination"));
                    flight.EconomyPrice = resultSet.getInt("EconomyPrice");
                    flights.add(flight);
                }

                senddata responseData = new senddata();
                responseData.status = "ok";
                responseData.res = flights;
                String jsonResponse = gson.toJson(responseData);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.write(jsonResponse);

            } catch (Exception ex) {
            senddata errorResponse = new senddata();
                errorResponse.status = "error";
            String errorJson = gson.toJson(errorResponse);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.write(errorJson);
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
        return "Short description";
    }

    class Flight {

        String flightId;
        String airline;
        String userid;
        String Departuredate;
        String departuretime;
        String departureFrom;
        String destination;
        int EconomyPrice;

        Flight(String flightId, String airline, String userid, String departuredate, String departuretime,
                String departureFrom, String destination) {
            this.flightId = flightId;
            this.airline = airline;
            this.userid = userid;
            this.Departuredate = departuredate;
            this.departuretime = departuretime;
            this.departureFrom = departureFrom;
            this.destination = destination;
        }
    }

    class data {

        String filter;
    }

    class senddata {

        String status;
        ArrayList<Flight> res;

        senddata() {
            res = new ArrayList<>();
        }
    }
}
