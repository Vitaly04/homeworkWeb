import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class HandlerClient implements Runnable {
    private final Socket socket;
    private final Map<String, Map<String, Handler>> handlers;

    public final Handler notFoundHandler = (request, out) -> {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
                ).getBytes());
                out.flush();
    };

    public HandlerClient(Socket socket, Map<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try (final var in = socket.getInputStream();
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = Request.parseInputStream(in);
            Map<String, Handler> handlerMap = handlers.get(request.getMethod());
            if (handlerMap == null) {
                notFoundHandler.handle(request, out);
                return;
            }
            Handler handler = handlerMap.get(request.getPath());
            if (handler == null) {
                notFoundHandler.handle(request, out);
                return;
            }
        handler.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
