package com.wealthassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LlamaService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/chat";
    private static final String MODEL_NAME = "llama3.2";
    private static final double TEMPERATURE = 1.0;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public LlamaService() {
        // 配置OkHttpClient，设置超时时间
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Check if Ollama service is available
     * @return true if service is available, false otherwise
     */
    public boolean isServiceAvailable() {
        try {
            Request request = new Request.Builder()
                .url("http://localhost:11434/api/tags")
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String generateAdvice(LocalDate startDate, LocalDate endDate, Map<String, Double> categoryExpenses, double totalIncome) {
        if (!isServiceAvailable()) {
            return "Error: Ollama service is not available. Please make sure to:\n" +
                   "1. Install Ollama from https://ollama.ai\n" +
                   "2. Run 'ollama serve' in terminal\n" +
                   "3. Run 'ollama pull llama3.2' to download the model\n" +
                   "4. Restart the application";
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an AI assistant for personal finance software, specialized in analyzing users' financial data and providing advice.\n\n");
        promptBuilder.append("Your main responsibilities are:\n");
        promptBuilder.append("1. Analyze users' income and spending patterns\n");
        promptBuilder.append("2. Provide specific money-saving suggestions\n");
        promptBuilder.append("3. Create reasonable budget plans\n");
        promptBuilder.append("4. Recommend suitable investment opportunities\n\n");
        
        promptBuilder.append("Here is the user's financial data:\n");
        promptBuilder.append(String.format("Time period: %s to %s\n", 
            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        promptBuilder.append(String.format("Total income: $%.2f\n\n", totalIncome));
        
        promptBuilder.append("Expense categories:\n");
        for (Entry<String, Double> entry : categoryExpenses.entrySet()) {
            promptBuilder.append(String.format("- %s: $%.2f\n", entry.getKey(), entry.getValue()));
        }
        
        promptBuilder.append("\nPlease provide the following analysis based on the above data:\n");
        promptBuilder.append("1. Spending pattern analysis: Evaluate if each expense is reasonable\n");
        promptBuilder.append("2. Money-saving suggestions: Provide specific saving strategies for each expense category\n");
        promptBuilder.append("3. Budget optimization: Create a more reasonable budget allocation plan\n");
        promptBuilder.append("4. Investment recommendations: Recommend suitable investment opportunities based on potential savings\n\n");
        promptBuilder.append("Please organize your response with a clear structure and bullet points.");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("temperature", TEMPERATURE);
        
        // Create messages array
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", promptBuilder.toString());
        messages.add(message);
        
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                .url(OLLAMA_API_URL)
                .post(body)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                // Extract message content from the response
                Map<String, Object> messageObj = (Map<String, Object>) responseMap.get("message");
                return (String) messageObj.get("content");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating advice: " + e.getMessage() + "\n\nPlease try again. If the problem persists, check if the Ollama service is running properly.";
        }
    }
}
