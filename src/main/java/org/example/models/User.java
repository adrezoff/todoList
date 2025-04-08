package org.example.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.bot.utils.UserState;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class User {
    private final long id;
    private String username;
    private boolean isPremium;
    private ZoneId timeZone;
    private UserState state;
    private Map<String, Object> tempData;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public User(long id) {
        this.id = id;
        this.tempData = new HashMap<>();
        this.state = UserState.IDLE;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
    public UserState getState() { return state; }

    public void setState(UserState state) { this.state = state; }
    public ZoneId getTimeZone() {
        return timeZone != null ? timeZone : ZoneId.of("Asia/Yekaterinburg");
    }
    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }
    public void setTimeZone(String zoneId) {
        this.timeZone = ZoneId.of(zoneId);
    }
    public String getTempDataJson() {
        try {
            // Создаем копию для сериализации, конвертируя LocalDateTime в строки
            Map<String, Object> serializableMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : tempData.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof LocalDateTime) {
                    serializableMap.put(entry.getKey(), value.toString());
                } else {
                    serializableMap.put(entry.getKey(), value);
                }
            }
            return objectMapper.writeValueAsString(serializableMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize tempData", e);
        }
    }

    public void setTempDataJson(String json) {
        try {
            if (json != null && !json.isEmpty()) {
                // Десериализуем в промежуточную карту
                Map<String, Object> rawMap = objectMapper.readValue(
                        json,
                        new TypeReference<HashMap<String, Object>>() {}
                );

                // Конвертируем строки обратно в LocalDateTime где нужно
                tempData.clear();
                for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // Если ключ содержит "Date" и значение строка - пробуем распарсить
                    if (value instanceof String && (key.contains("Date") || key.contains("date"))) {
                        try {
                            tempData.put(key, LocalDateTime.parse((String) value));
                        } catch (Exception e) {
                            tempData.put(key, value); // Оставляем как строку, если не получилось распарсить
                        }
                    } else {
                        tempData.put(key, value);
                    }
                }
            } else {
                tempData = new HashMap<>();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize tempData", e);
        }
    }
    public void putTempData(String key, Object value) {
        tempData.put(key, value);
    }
    public void clearTempData(){
        tempData.clear();
    }

    public Object getTempData(String key) {
        return tempData.get(key);
    }
}