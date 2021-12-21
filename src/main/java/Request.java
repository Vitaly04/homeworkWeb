import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream body;
    private List<NameValuePair> nameValuePairs;

    private Request(String method, String path, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
       return URLEncodedUtils.parse(getQuery(), StandardCharsets.UTF_8);
    }

    private String getQuery() {
        var i = path.indexOf("?");
        return path.substring(i+1);
    }

    public String getQueryParam(String name) {
        nameValuePairs = URLEncodedUtils.parse(getQuery(), StandardCharsets.UTF_8);
        for (NameValuePair param : nameValuePairs) {
            if (name.equals(param.getName())) return param.getValue();
        }
        return "parameter: " + name +" not found" ;
    }

    public static Request parseInputStream(InputStream inputStream) throws IOException {
        final var in = new BufferedReader(new InputStreamReader(inputStream));
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request");
        }
        final var method = parts[0];
        final var path = parts[1];
        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            int i = line.indexOf(":");
            String name = line.substring(0, i);
            String value = line.substring(i + 2);
            headers.put(name, value);
        }

        return new Request(method, path, headers, inputStream);
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                '}';
    }
}
