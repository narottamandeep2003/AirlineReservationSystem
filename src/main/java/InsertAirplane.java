import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/insertAirplane"})
public class InsertAirplane extends HttpServlet {

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
        AirplaneData data = gson.fromJson(jsonBuffer.toString(), AirplaneData.class);

        String airline = data.getAirline();
        String model = data.getModel();
        int economyRow = data.getEconomyRow();
        int economyCol = data.getEconomyCol();
        int businessRow = data.getBusinessRow();
        int businessCol = data.getBusinessCol();
        String userid = data.getUserid();
        
        String dbUrl = "jdbc:mysql://localhost:3306/airline_db";
        String dbUserName = "root";
        String dbPassword = "user";
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
                String insertFlightQuery = "INSERT INTO airplanes (airline, userid, model, EconomyRow, EconomyColumn, BusinessRow, BusinessColumn) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertFlightQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, airline);
                    statement.setString(2, userid);
                    statement.setString(3, model);
                    statement.setInt(4, economyRow);
                    statement.setInt(5, economyCol);
                    statement.setInt(6, businessRow);
                    statement.setInt(7, businessCol);
                    int affectedRows = statement.executeUpdate();
                    if (affectedRows == 0) {
                        sendResponse(response, "error");
                    }
                    try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int generatedId = generatedKeys.getInt(1);
                            System.out.println("Inserted Plane ID: " + generatedId);
                            sendResponse(response, "ok");
                        } else {
                            sendResponse(response, "error");
                        }
                    }
                }
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            sendErrorResponse(response, e);
        }
    }

    static class AirplaneData {
        private String airline;
        private String model;
        private int EconomyRow;
        private int EconomyCol;
        private int BusinessRow;
        private int BusinessCol;
        private String userid;
        
        // Getters
        public String getAirline() {
            return airline;
        }

        public String getModel() {
            return model;
        }

        public int getEconomyRow() {
            return EconomyRow;
        }

        public int getEconomyCol() {
            return EconomyCol;
        }

        public int getBusinessRow() {
            return BusinessRow;
        }

        public int getBusinessCol() {
            return BusinessCol;
        }

        public String getUserid() {
            return userid;
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

    private void sendResponse(HttpServletResponse response, String status) throws IOException {
        responseObject res = new responseObject();
        res.status = status;
        Gson gson = new Gson();
        String json = gson.toJson(res);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private void sendErrorResponse(HttpServletResponse response, Exception e) throws IOException {
        responseObject res = new responseObject();
        res.status = "error";
        res.data = e.getLocalizedMessage();
        Gson gson = new Gson();
        String json = gson.toJson(res);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    static class responseObject {
        public String status;
        public String data;
    }
}
