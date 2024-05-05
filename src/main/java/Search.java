import com.google.gson.Gson;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/Search"})
public class Search extends HttpServlet {

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

            SearchTag data = gson.fromJson(jsonBuffer.toString(), SearchTag.class);

            String db_url = "jdbc:mysql://localhost:3306/airline_db";
            String db_userName = "root";
            String db_password = "user";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(db_url, db_userName, db_password);

            String filter = data.search != null ? data.search : "";

            String query =
                    "SELECT * FROM (SELECT flightId,EconomyPrice, airline,airplanes.userid,airplanes.airplaneId, Departuredate, departureFrom, destination, Departuretime FROM flights LEFT JOIN airplanes ON airplanes.airplaneId = flights.airplaneId ) AS f WHERE (f.departureDate > ? OR (f.departureDate = ? AND f.departureTime > ? ))";
            statement = connection.prepareStatement(query);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
            String currDate = dateFormat.format(new Date());

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String currTime = timeFormat.format(new Date());

            statement.setString(1, currDate);
            statement.setString(2, currDate);
            statement.setString(3, currTime);

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

            if ("LowToHigh".equals(data.sort)) {
                flights = filterAndSortFlights(flights, filter, Comparator.comparingInt(f -> f.EconomyPrice));
            } else if ("HighToLow".equals(data.sort)) {
                flights = filterAndSortFlights(flights, filter, Comparator.comparingInt((Flight f) -> f.EconomyPrice).reversed());
            } else {
                flights = filterFlights(flights, filter);
            }

            SendData responseData = new SendData();
            responseData.status = "ok";
            responseData.res = flights;

            String jsonResponse = gson.toJson(responseData);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write(jsonResponse);

        } catch (Exception ex) {
            SendData errorResponse = new SendData();
            errorResponse.status = "error";
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

    private ArrayList<Flight> filterAndSortFlights(ArrayList<Flight> flights, String filter, Comparator<Flight> comparator) {
        return filterFlights(flights, filter, true, comparator);
    }

    private ArrayList<Flight> filterFlights(ArrayList<Flight> flights, String filter) {
        return filterFlights(flights, filter, false, null);
    }

    private ArrayList<Flight> filterFlights(ArrayList<Flight> flights, String filter, boolean doSort, Comparator<Flight> comparator) {
        ArrayList<Flight> filteredFlights = new ArrayList<>();
        for (Flight flight : flights) {
            if (flightMatchesFilter(flight, filter)) {
                filteredFlights.add(flight);
            }
        }
        if (doSort && comparator != null) {
            filteredFlights.sort(comparator);
        }
        return filteredFlights;
    }

    private boolean flightMatchesFilter(Flight flight, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        // You need to implement your filter logic here based on flight attributes
        return flight.destination.contains(filter) || flight.departureFrom.contains(filter) || flight.airline.contains(filter);
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

    class SearchTag {
        String search;
        String sort;
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

    class SendData {
        String status;
        ArrayList<Flight> res;

        SendData() {
            res = new ArrayList<>();
        }
    }
}
