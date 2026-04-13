package ru.hofftech.omni.shipping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TelegramShippingBotApp {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "createpackage",
            "findpackage",
            "deletepackage",
            "load",
            "unload"
    );

    public static void main(String[] args) throws Exception {
        Map<String, String> dotEnv = loadDotEnv(Path.of(".env"));

        String token = getSetting("TELEGRAM_BOT_TOKEN", dotEnv);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Не задан TELEGRAM_BOT_TOKEN в .env");
        }

        String allowedChatId = getSetting("TELEGRAM_ALLOWED_CHAT_ID", dotEnv);
        TelegramClient client = new TelegramClient(token);

        long offset = 0L;
        while (true) {
            List<Update> updates = client.getUpdates(offset, 30);
            for (Update update : updates) {
                offset = Math.max(offset, update.updateId + 1);
                if (update.message == null || update.message.text == null || update.message.chat == null) {
                    continue;
                }

                String chatId = String.valueOf(update.message.chat.id);
                if (allowedChatId != null && !allowedChatId.isBlank() && !allowedChatId.equals(chatId)) {
                    continue;
                }

                String response = processText(update.message.text);
                client.sendMessage(chatId, response);
            }
        }
    }

    private static String processText(String text) {
        String normalized = text == null ? "" : text.trim();
        if (normalized.isEmpty()) {
            return "Пустая команда.";
        }

        if (normalized.equalsIgnoreCase("/start") || normalized.equalsIgnoreCase("/help")) {
            return """
                    Поддерживаемые команды:
                    createpackage
                    findpackage
                    deletepackage
                    load
                    unload

                    Пример:
                    createpackage -name "test4x4" -form "oooo\\no  o\\no  o\\noooo\\n"
                    """;
        }

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        List<String> tokens = ShippingApp.tokenizeCommandLine(normalized);
        if (tokens.isEmpty()) {
            return "Пустая команда.";
        }

        String command = tokens.get(0).toLowerCase(Locale.ROOT);
        if (!ALLOWED_COMMANDS.contains(command)) {
            return "Разрешены только команды: createpackage, findpackage, deletepackage, load, unload";
        }

        String result = ShippingApp.executeExternalCommandLine(normalized);
        return trimForTelegram(result);
    }

    private static String trimForTelegram(String text) {
        if (text == null || text.isBlank()) {
            return "Команда выполнена.";
        }
        if (text.length() <= 4000) {
            return text;
        }
        return text.substring(0, 4000) + "\n... (output truncated)";
    }

    private static String getSetting(String key, Map<String, String> dotEnv) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return dotEnv.get(key);
    }

    private static Map<String, String> loadDotEnv(Path envPath) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        if (!Files.exists(envPath)) {
            return result;
        }

        List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
        for (String rawLine : lines) {
            if (rawLine == null) {
                continue;
            }
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }

            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            result.put(key, stripWrappingQuotes(value));
        }
        return result;
    }

    private static String stripWrappingQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static class TelegramClient {
        private final HttpClient httpClient;
        private final ObjectMapper mapper;
        private final String baseUrl;

        TelegramClient(String token) {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            this.mapper = new ObjectMapper();
            this.baseUrl = "https://api.telegram.org/bot" + token + "/";
        }

        List<Update> getUpdates(long offset, int timeoutSeconds) throws IOException, InterruptedException {
            String url = baseUrl + "getUpdates?offset=" + offset + "&timeout=" + timeoutSeconds;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds + 10L))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            ApiResponse<List<Update>> apiResponse = mapper.readValue(
                    response.body(),
                    mapper.getTypeFactory().constructParametricType(
                            ApiResponse.class,
                            mapper.getTypeFactory().constructCollectionType(List.class, Update.class)
                    )
            );

            if (!apiResponse.ok) {
                throw new IllegalStateException("Ошибка Telegram API при getUpdates");
            }
            return apiResponse.result != null ? apiResponse.result : List.of();
        }

        void sendMessage(String chatId, String text) throws IOException, InterruptedException {
            String payload = "chat_id=" + encode(chatId) + "&text=" + encode(text);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "sendMessage"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            ApiResponse<Object> apiResponse = mapper.readValue(
                    response.body(),
                    mapper.getTypeFactory().constructParametricType(ApiResponse.class, Object.class)
            );
            if (!apiResponse.ok) {
                throw new IllegalStateException("Ошибка Telegram API при sendMessage");
            }
        }

        private String encode(String value) {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        @JsonProperty("ok")
        public boolean ok;
        @JsonProperty("result")
        public T result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Update {
        @JsonProperty("update_id")
        public long updateId;
        @JsonProperty("message")
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message {
        @JsonProperty("chat")
        public Chat chat;
        @JsonProperty("text")
        public String text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Chat {
        @JsonProperty("id")
        public long id;
    }
}
