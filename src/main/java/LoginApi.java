
import java.io.BufferedReader;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


class responseObject {

    String status;
    String data;
    String id;
    String username;
    String email;
    String role;
}

@WebServlet(urlPatterns = {"/LoginApi"})
public class LoginApi extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder jsonBuffer;

        try ( // Get the input stream from the request to read the JSON data
                BufferedReader reader = request.getReader()) {
            // Create a StringBuilder to store the JSON data
            jsonBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }

        }

        Gson gson = new Gson();
        LoginData loginData = gson.fromJson(jsonBuffer.toString(), LoginData.class);

        String email = loginData.getEmail();
        String password = loginData.getPassword();

        String db_url = "jdbc:mysql://localhost:3306/airline_db";
        String db_userName = "root";
        String db_password = "user";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(db_url, db_userName, db_password);
            String searchUser = "select id,username,email,role from users where email=? and password=?";
            PreparedStatement statement = connection.prepareStatement(searchUser);
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet userdetail = statement.executeQuery();
            responseObject res = new responseObject();
            if (userdetail.next()) {
                res.status = "ok";
                res.id = userdetail.getString(1);
                res.username = userdetail.getString(2);
                res.email = userdetail.getString(3);
                res.role = userdetail.getString(4);
                String json = gson.toJson(res);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            } else {
                res.status = "error";
                String json = gson.toJson(res);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            }
        } catch (IOException | SQLException e) {
            responseObject res = new responseObject();
            res.status = "error";
            res.data = e.getLocalizedMessage();
            String json = gson.toJson(res);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } catch (ClassNotFoundException ex) {
            responseObject res = new responseObject();
            res.status = "error";
            res.data = ex.getLocalizedMessage();
            String json = gson.toJson(res);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        }

    }

    // Define a Java class to represent the JSON data
    static class LoginData {

        private String email;
        private String password;

        // Getters and setters
        public String getEmail() {
            return email;
        }

        public void setUsername(String username) {
            this.email = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
