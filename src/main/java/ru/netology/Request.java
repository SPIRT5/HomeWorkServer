package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, List<String>> queryParams;

    public Request(BufferedReader in) throws IOException {
        // первая строка запроса: GET /messages?last=10 HTTP/1.1
        final String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }

        final String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        this.method = parts[0];
        String fullPath = parts[1];
        this.version = parts[2];

        // Парсим path и query
        String rawPath;
        Map<String, List<String>> parsedQueryParams = new HashMap<>();
        if (fullPath.contains("?")) {
            rawPath = fullPath.substring(0, fullPath.indexOf("?"));
            String query = fullPath.substring(fullPath.indexOf("?") + 1);
            List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
            for (NameValuePair pair : pairs) {
                parsedQueryParams
                        .computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                        .add(pair.getValue());
            }
        } else {
            rawPath = fullPath;
        }
        this.path = rawPath;
        this.queryParams = parsedQueryParams;

        // Заголовки
        Map<String, String> tempHeaders = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(":");
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                tempHeaders.put(name, value);
            }
        }
        this.headers = tempHeaders;

        // Тело (если есть Content-Length)
        String tempBody = null;
        if (headers.containsKey("Content-Length")) {
            int length = Integer.parseInt(headers.get("Content-Length"));
            char[] buf = new char[length];
            int read = in.read(buf);
            tempBody = new String(buf, 0, read);
        }
        this.body = tempBody;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public Optional<String> getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        if (values != null && !values.isEmpty()) {
            return Optional.of(values.get(0));
        }
        return Optional.empty();
    }

    public List<String> getQueryParams(String name) {
        return queryParams.getOrDefault(name, List.of());
    }
}
