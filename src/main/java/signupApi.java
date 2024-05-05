import com.google.gson.Gson;
import java.io.IOException;
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

@WebServlet(urlPatterns = {"/signupApi"})
public class signupApi extends HttpServlet {

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
        registerData registerData = gson.fromJson(jsonBuffer.toString(), registerData.class);

        String email = registerData.getEmail();
        String password = registerData.getPassword();
        String role = registerData.getRole();
        String gender = registerData.getGender();
        String username = registerData.getUsername();
        String db_url = "jdbc:mysql://localhost:3306/airline_db";
        String db_userName = "root";
        String db_password = "user";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(db_url, db_userName, db_password);
            String insertUser = "INSERT INTO users(username, email, password, role, Gender) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(insertUser);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, role);
            statement.setString(5, gender);
            int result = statement.executeUpdate();
            if (result == 1) {
                System.out.println("Registered Successfully");
                responseObject res = new responseObject();
                res.status = "ok";

                String json = gson.toJson(res);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            } else {
                System.out.println("Not Registered");
                responseObject res = new responseObject();
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

    static class registerData {

        private String email;
        private String password;
        private String gender;
        private String role;
        private String username;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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
}
