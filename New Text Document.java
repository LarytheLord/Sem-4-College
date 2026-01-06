import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeminiChat {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";

    public static void main(String[] args) throws Exception {

        if (API_KEY == null) {
            System.out.println("❌ GEMINI_API_KEY environment variable not set.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("🤖 Gemini Q&A (type 'exit' to quit)\n");

        while (true) {
            System.out.print("You: ");
            String question = scanner.nextLine();

            if (question.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye 👋");
                break;
            }

            String response = askGemini(question);
            System.out.println("\nGemini: " + response + "\n");
        }

        scanner.close();
    }

    private static String askGemini(String prompt) throws Exception {

        URL url = new URL(API_URL + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = """
        {
          "contents": [{
            "parts": [{
              "text": "%s"
            }]
          }]
        }
        """.formatted(prompt.replace("\"", "\\\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // Simple text extraction (basic)
        String result = response.toString();
        int textIndex = result.indexOf("\"text\":\"");
        if (textIndex != -1) {
            int start = textIndex + 8;
            int end = result.indexOf("\"", start);
            return result.substring(start, end).replace("\\n", "\n");
        }

        return "No response received.";
    }
}
