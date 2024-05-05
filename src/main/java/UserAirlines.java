import com.google.gson.Gson;
import java.io.IOException;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/UserAirlines"})
public class UserAirlines extends HttpServlet {

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
        User user = gson.fromJson(jsonBuffer.toString(), User.class);
        String userId = user.getUserId();
        String dbUrl = "jdbc:mysql://localhost:3306/airline_db";
        String dbUserName = "root";
        String dbPassword = "user";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
                ArrayList<AirlinePair> responseList = new ArrayList<>();
                PreparedStatement statement = connection.prepareStatement("select airplaneId,model from airplanes where userid=? ");
                statement.setString(1, userId);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    responseList.add(new AirlinePair(rs.getString(1), rs.getString(2)));
                }
                ResponseObject res = new ResponseObject();
                res.status = "ok";
                res.airplanes = responseList;
                
                String jsonResponse = gson.toJson(res);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            sendErrorResponse(response, e);
        }
    }

    static class User {
        String userid;

        public String getUserId() {
            return userid;
        }

        public void setUserId(String userId) {
            this.userid = userId;
        }
    }

    static class AirlinePair {
        String airplaneId;
        String model;

        public AirlinePair(String airplaneId, String model) {
            this.airplaneId = airplaneId;
            this.model = model;
        }
    }

    static class ResponseObject {
        String status;
        ArrayList<AirlinePair> airplanes;
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

    private void sendErrorResponse(HttpServletResponse response, Exception e) throws IOException {
        ResponseObject res = new ResponseObject();
        res.status = "error";
        res.airplanes = null; // Set to null to indicate error
        Gson gson = new Gson();
        String json = gson.toJson(res);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
