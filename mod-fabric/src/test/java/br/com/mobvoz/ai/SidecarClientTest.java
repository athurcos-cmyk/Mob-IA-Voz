package br.com.mobvoz.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SidecarClientTest {
    private static final String OK = "{\"speech_text\":\"Muuu! Gosto de capim.\",\"language\":\"pt-BR\",\"emotion\":\"alegre\","
            + "\"intent\":\"conversar\",\"proposals\":[{\"type\":\"REGISTER_CLAIM\",\"parameters\":{\"text\":\"sou seu pai\"}}],\"source\":\"llm\",\"llm_ms\":42}";

    private static HttpServer stub(int status, String body) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        s.createContext("/v1/reply", ex -> {
            byte[] b = body.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(status, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        s.start();
        return s;
    }

    private static SidecarClient clientFor(HttpServer s) {
        return new SidecarClient("http://127.0.0.1:" + s.getAddress().getPort(), null, Duration.ofSeconds(5));
    }

    @Test
    void parsesValidReply() throws Exception {
        HttpServer s = stub(200, OK);
        try {
            var r = clientFor(s).reply("minecraft:cow", "oi", List.of("EMOTE"), List.of()).get(5, TimeUnit.SECONDS);
            assertEquals("Muuu! Gosto de capim.", r.speechText());
            assertEquals("REGISTER_CLAIM", r.proposals().get(0).type());
            assertEquals("sou seu pai", r.proposals().get(0).params().get("text"));
            assertEquals("llm", r.source());
        } finally {
            s.stop(0);
        }
    }

    /** T06: sidecar desligado -> o future completa (sem excecao, sem travar) como offline. */
    @Test
    void offlineSidecarCompletesGracefully() throws Exception {
        int freePort;
        try (ServerSocket ss = new ServerSocket(0)) {
            freePort = ss.getLocalPort();
        }
        var c = new SidecarClient("http://127.0.0.1:" + freePort, null, Duration.ofSeconds(5));
        long t0 = System.nanoTime();
        var r = c.reply("minecraft:cow", "oi", List.of(), List.of()).get(5, TimeUnit.SECONDS);
        assertTrue(r.offline());
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 3000);
    }

    @Test
    void garbageAndErrorsBecomeOffline() throws Exception {
        for (var pair : List.of(new Object[] {200, "isto nao e json"}, new Object[] {500, "{}"}, new Object[] {200, "{\"speech_text\":\"\"}"})) {
            HttpServer s = stub((Integer) pair[0], (String) pair[1]);
            try {
                assertTrue(clientFor(s).reply("minecraft:cow", "oi", List.of(), List.of()).get(5, TimeUnit.SECONDS).offline());
            } finally {
                s.stop(0);
            }
        }
    }
}
