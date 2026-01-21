import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123";
        String dbPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKCP6ene";

        boolean matches = encoder.matches(rawPassword, dbPassword);
        System.out.println("Password matches: " + matches);

        // 生成新的密码哈希
        String newHash = encoder.encode(rawPassword);
        System.out.println("New hash: " + newHash);
    }
}
