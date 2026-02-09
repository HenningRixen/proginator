package com.example.prog1learnapp.service.lsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class LspJsonRpcFraming {
    private static final String CONTENT_LENGTH = "Content-Length:";

    private LspJsonRpcFraming() {
    }

    public static void writeMessage(OutputStream outputStream, String payload) throws IOException {
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        String header = CONTENT_LENGTH + " " + body.length + "\r\n\r\n";
        outputStream.write(header.getBytes(StandardCharsets.US_ASCII));
        outputStream.write(body);
        outputStream.flush();
    }

    public static String readMessage(InputStream inputStream) throws IOException {
        int contentLength = -1;

        while (true) {
            String line = readAsciiLine(inputStream);
            if (line == null) {
                return null;
            }
            if (line.isEmpty()) {
                break;
            }
            if (line.regionMatches(true, 0, CONTENT_LENGTH, 0, CONTENT_LENGTH.length())) {
                String value = line.substring(CONTENT_LENGTH.length()).trim();
                contentLength = Integer.parseInt(value);
            }
        }

        if (contentLength < 0) {
            throw new IOException("Missing Content-Length header in LSP message");
        }

        byte[] payload = inputStream.readNBytes(contentLength);
        if (payload.length != contentLength) {
            return null;
        }
        return new String(payload, StandardCharsets.UTF_8);
    }

    private static String readAsciiLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        while (true) {
            int next = inputStream.read();
            if (next == -1) {
                if (buffer.size() == 0) {
                    return null;
                }
                break;
            }
            if (next == '\n') {
                break;
            }
            if (next != '\r') {
                buffer.write(next);
            }
        }

        return buffer.toString(StandardCharsets.US_ASCII);
    }
}
