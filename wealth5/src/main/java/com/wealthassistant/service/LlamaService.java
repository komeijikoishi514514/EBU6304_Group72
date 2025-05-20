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
        promptBuilder.append("你是一款个人财务软件的AI助手，专门负责分析用户的财务数据并提供建议。\n\n");
        promptBuilder.append("你的主要职责是：\n");
        promptBuilder.append("1. 分析用户的收入和支出模式\n");
        promptBuilder.append("2. 提供具体的省钱建议\n");
        promptBuilder.append("3. 制定合理的预算计划\n");
        promptBuilder.append("4. 推荐适合的投资机会\n\n");
        
        promptBuilder.append("以下是用户的财务数据：\n");
        promptBuilder.append(String.format("时间范围：%s 至 %s\n", 
            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        promptBuilder.append(String.format("总收入：$%.2f\n\n", totalIncome));
        
        promptBuilder.append("支出分类：\n");
        for (Entry<String, Double> entry : categoryExpenses.entrySet()) {
            promptBuilder.append(String.format("- %s：$%.2f\n", entry.getKey(), entry.getValue()));
        }
        
        promptBuilder.append("\n请根据以上数据，提供以下分析：\n");
        promptBuilder.append("1. 支出模式分析：评估各项支出是否合理\n");
        promptBuilder.append("2. 省钱建议：针对每个支出类别提供具体的省钱方案\n");
        promptBuilder.append("3. 预算优化：制定更合理的预算分配计划\n");
        promptBuilder.append("4. 投资建议：基于可能的节省金额，推荐合适的投资机会\n\n");
        promptBuilder.append("请用清晰的结构和要点来组织你的回答。");

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