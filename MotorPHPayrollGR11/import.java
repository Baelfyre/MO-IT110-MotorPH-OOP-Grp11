import java.io.File;

public class CheckFileExample {
    public static void main(String[] args) {
        File file = new File("EmployeeData.csv");

        if (file.exists()) {
            System.out.println("File found: " + file.getAbsolutePath());
        } else {
            System.out.println("File not found.");
        }
    }
}