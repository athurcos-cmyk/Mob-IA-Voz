package br.com.mobvoz.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cliente HTTP assincrono do sidecar de IA (127.0.0.1). Nunca bloqueia o tick: devolve um CompletableFuture
 * que SEMPRE completa (com {@link Reply#unavailable()} se o sidecar estiver fora do ar).
 * O texto devolvido e apenas proposta: quem valida/executa e o servidor do jogo.
 */
public final class SidecarClient {
    public static final String DEFAULT_URL = "http://127.0.0.1:8765";
    private static final Gson GSON = new Gson();

    /** @param source "llm", "fallback" (sidecar vivo mas LLM falhou) ou "offline" (sidecar inacessivel). */
    /** Proposta do LLM: so um pedido. Quem decide se vira efeito e o servidor. */
    public record Proposal(String type, Map<String, Object> params) {
    }

    public record Reply(String speechText, String emotion, String intent, List<Proposal> proposals, String source, long llmMs) {
        public static Reply unavailable() {
            return new Reply("", "neutro", "conversar", List.of(), "offline", 0);
        }

        public boolean offline() {
            return "offline".equals(source);
        }
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final String baseUrl;
    private final String token;
    private final Duration timeout;

    public SidecarClient(String baseUrl, String token, Duration timeout) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.timeout = timeout;
    }

    public static SidecarClient local() {
        String url = System.getProperty("mobvoz.sidecar", DEFAULT_URL);
        return new SidecarClient(url, System.getenv("MOBVOZ_TOKEN"), Duration.ofSeconds(45));
    }

    /** Pedido de resposta falada. {@code capabilities} vem SEMPRE do servidor, nunca do jogador/LLM. */
    public CompletableFuture<Reply> reply(String entityType, String playerText, List<String> capabilities, List<String> facts) {
        return reply(entityType, playerText, capabilities, facts, List.of(), Map.of());
    }

    /** {@code facts} = eventos VERIFICADOS; {@code claims} = o que o jogador disse (nao e fato); {@code relation} = numeros do servidor. */
    public CompletableFuture<Reply> reply(String entityType, String playerText, List<String> capabilities, List<String> facts,
                                          List<String> claims, Map<String, Double> relation) {
        return reply(entityType, playerText, capabilities, facts, claims, relation, null, List.of());
    }

    public record SceneLine(int speaker, String text) {
    }

    /** Cena curta entre duas criaturas do mesmo tipo (so fala). Falha/offline = lista vazia (o chamador usa o fallback). */
    public CompletableFuture<List<SceneLine>> scene(String entityType, String label, String style, int maxTurns) {
        JsonObject body = new JsonObject();
        body.addProperty("entity_type", entityType);
        body.addProperty("label", label);
        body.addProperty("style", style);
        body.addProperty("max_turns", maxTurns);
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/scene"))
                .timeout(timeout).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        auth(rb);
        return http.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    List<SceneLine> out = new ArrayList<>();
                    if (r.statusCode() == 200) {
                        for (JsonElement e : GSON.fromJson(r.body(), JsonObject.class).getAsJsonArray("lines")) {
                            JsonObject o = e.getAsJsonObject();
                            out.add(new SceneLine(o.get("speaker").getAsInt(), o.get("text").getAsString()));
                        }
                    }
                    return out;
                })
                .exceptionally(t -> List.of());
    }

    /** Alegacao + ordens detectadas em linguagem livre (passo 1); so SUGEREM, o servidor decide. */
    public record Item(String name, int count) {
    }

    /** @param ask o que o jogador PEDE; @param offers o que diz que daria; @param persuadeItem/Strength o argumento para a criatura querer algo. */
    public record Analysis(String claim, List<String> orders, Item ask, List<Item> offers, String persuadeItem, int persuadeStrength, String social, int haggle, int appeal) {
        public static final Analysis EMPTY = new Analysis("", List.of(), new Item("", 0), List.of(), "", 0, "NONE", 0, 0);

        public Analysis(String claim, List<String> orders) {
            this(claim, orders, new Item("", 0), List.of(), "", 0, "NONE", 0, 0);
        }
    }

    public CompletableFuture<Analysis> analyze(String playerText, List<String> capabilities) {
        JsonObject body = new JsonObject();
        body.addProperty("text", playerText);
        body.add("capabilities", GSON.toJsonTree(capabilities));
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/analyze"))
                .timeout(timeout).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        auth(rb);
        return http.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (r.statusCode() != 200) {
                        return Analysis.EMPTY;
                    }
                    JsonObject o = GSON.fromJson(r.body(), JsonObject.class);
                    List<String> orders = new ArrayList<>();
                    for (JsonElement e : o.getAsJsonArray("orders")) {
                        orders.add(e.getAsString());
                    }
                    Item ask = new Item("", 0);
                    List<Item> offers = new ArrayList<>();
                    String pItem = "";
                    int pStr = 0;
                    try {
                        if (o.has("ask") && o.get("ask").isJsonObject()) {
                            JsonObject a = o.getAsJsonObject("ask");
                            ask = new Item(a.get("item").getAsString(), Math.max(0, Math.min(a.get("count").getAsInt(), 64)));
                        }
                        if (o.has("offers") && o.get("offers").isJsonArray()) {
                            for (JsonElement e : o.getAsJsonArray("offers")) {
                                JsonObject x = e.getAsJsonObject();
                                offers.add(new Item(x.get("item").getAsString(), Math.max(1, Math.min(x.get("count").getAsInt(), 64))));
                            }
                        }
                        if (o.has("persuasion") && o.get("persuasion").isJsonObject()) {
                            JsonObject x = o.getAsJsonObject("persuasion");
                            pItem = x.get("item").getAsString();
                            pStr = Math.max(0, Math.min(x.get("strength").getAsInt(), 2));
                        }
                    } catch (RuntimeException ignored) {
                        // campos de negociacao mal formados: ignora so eles
                    }
                    String social = o.has("social") && o.get("social").isJsonPrimitive() ? o.get("social").getAsString() : "NONE";
                    int haggle = 0;
                    try {
                        haggle = o.has("haggle") ? Math.max(0, Math.min(o.get("haggle").getAsInt(), 2)) : 0;
                    } catch (RuntimeException ignored) {
                        // haggle mal formado: sem barganha
                    }
                    int appeal = 0;
                    try {
                        appeal = o.has("appeal") ? Math.max(0, Math.min(o.get("appeal").getAsInt(), 2)) : 0;
                    } catch (RuntimeException ignored) {
                        // appeal mal formado: sem argumento
                    }
                    return new Analysis(o.get("claim").getAsString(), orders, ask, offers, pItem, pStr,
                            social.equals("THREAT") || social.equals("COMFORT") ? social : "NONE", haggle, appeal);
                })
                .exceptionally(t -> Analysis.EMPTY);
    }

    /** Julgamento da IA (so propoe): {@code accept} por NOME falado -> YES|MAYBE|NO; {@code give} para o item pedido. Vazio = sem IA. */
    public record Judgment(Map<String, String> accept, String give) {
        public static final Judgment NONE = new Judgment(Map.of(), "");
    }

    public CompletableFuture<Judgment> judge(Map<String, String> profile, List<String> accept, String give) {
        JsonObject body = new JsonObject();
        body.add("profile", GSON.toJsonTree(profile));
        body.add("accept", GSON.toJsonTree(accept));
        body.addProperty("give", give);
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/judge"))
                .timeout(timeout).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        auth(rb);
        return http.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (r.statusCode() != 200) {
                        return Judgment.NONE;
                    }
                    JsonObject o = GSON.fromJson(r.body(), JsonObject.class);
                    if (o == null || !o.has("accept") || !o.get("accept").isJsonObject()) {
                        return Judgment.NONE;
                    }
                    Map<String, String> acc = new java.util.HashMap<>();
                    for (var e : o.getAsJsonObject("accept").entrySet()) {
                        acc.put(e.getKey(), e.getValue().getAsString());
                    }
                    return new Judgment(acc, o.has("give") ? o.get("give").getAsString() : "");
                })
                .exceptionally(t -> Judgment.NONE);
    }

    /** @param detectedClaim null = o sidecar analisa sozinho; "" = o servidor ja analisou e nao ha alegacao. */
    public CompletableFuture<Reply> reply(String entityType, String playerText, List<String> capabilities, List<String> facts,
                                          List<String> claims, Map<String, Double> relation, String detectedClaim,
                                          List<Map<String, String>> orderResults) {
        return reply(entityType, playerText, capabilities, facts, claims, relation, detectedClaim, orderResults, Map.of());
    }

    public CompletableFuture<Reply> reply(String entityType, String playerText, List<String> capabilities, List<String> facts,
                                          List<String> claims, Map<String, Double> relation, String detectedClaim,
                                          List<Map<String, String>> orderResults, Map<String, String> profile) {
        return reply(entityType, playerText, capabilities, facts, claims, relation, detectedClaim, orderResults, profile, List.of());
    }

    /** {@code history}: ultimas trocas desta conversa ({"jogador","voce"}), para a criatura continuar o assunto. */
    public CompletableFuture<Reply> reply(String entityType, String playerText, List<String> capabilities, List<String> facts,
                                          List<String> claims, Map<String, Double> relation, String detectedClaim,
                                          List<Map<String, String>> orderResults, Map<String, String> profile,
                                          List<Map<String, String>> history) {
        JsonObject body = new JsonObject();
        body.add("history", GSON.toJsonTree(history));
        body.add("profile", GSON.toJsonTree(profile));
        if (detectedClaim != null) {
            body.addProperty("detected_claim", detectedClaim);
        }
        body.add("order_results", GSON.toJsonTree(orderResults));
        body.add("claims", GSON.toJsonTree(claims));
        body.add("relation", GSON.toJsonTree(relation));
        body.addProperty("entity_type", entityType);
        body.addProperty("text", playerText);
        body.add("capabilities", GSON.toJsonTree(capabilities));
        body.add("facts", GSON.toJsonTree(facts));
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/reply"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        auth(rb);
        return http.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> r.statusCode() == 200 ? parse(r.body()) : Reply.unavailable())
                .exceptionally(t -> Reply.unavailable());
    }

    /** Estado resumido do sidecar para /mobvoz status ("online (Ollama ok)", "online (Ollama fora)", "offline"). */
    public CompletableFuture<String> health() {
        HttpRequest rq = HttpRequest.newBuilder(URI.create(baseUrl + "/health")).timeout(Duration.ofSeconds(4)).GET().build();
        return http.sendAsync(rq, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    JsonObject o = GSON.fromJson(r.body(), JsonObject.class);
                    return "online (Ollama " + (o.get("ollama").getAsBoolean() ? "ok" : "fora") + ", modelo " + o.get("model").getAsString() + ")";
                })
                .exceptionally(t -> "offline");
    }

    /** PCM int16 48 kHz mono -> texto pt. Vazio se o sidecar/STT estiver fora do ar ou nao entender nada. */
    public CompletableFuture<Optional<String>> transcribe(byte[] pcm48k) {
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/transcribe"))
                .timeout(timeout)
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(pcm48k));
        auth(rb);
        return http.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (r.statusCode() != 200) {
                        return Optional.<String>empty();
                    }
                    String t = GSON.fromJson(r.body(), JsonObject.class).get("text").getAsString().trim();
                    return t.isEmpty() ? Optional.<String>empty() : Optional.of(t);
                })
                .exceptionally(t -> Optional.empty());
    }

    /** Audio das falas FIXAS ja sintetizadas (48 MB no maximo): a mesma frase com a mesma voz sai na hora, sem chamar o TTS de novo. */
    public static final AudioCache AUDIO = new AudioCache(48L * 1024 * 1024);

    /** Como {@link #synthesize}, mas com cache: so para falas fixas (as da IA sao unicas). O tom individual e arredondado em degraus de 2%. */
    public CompletableFuture<Optional<short[]>> synthesizeCached(String text, String voice, double pitch, double speed) {
        double p = AudioCache.quantizePitch(pitch);
        String key = AudioCache.key(text, voice, p, speed, br.com.mobvoz.config.ServerConfig.current().voiceEngine);
        short[] hit = AUDIO.get(key);
        if (hit != null) {
            return CompletableFuture.completedFuture(Optional.of(hit));
        }
        return synthesize(text, voice, p, speed).thenApply(o -> {
            o.ifPresent(pcm -> AUDIO.put(key, pcm));
            return o;
        });
    }

    /** Texto pt-BR -> PCM int16 48 kHz mono (como short[]). Vazio se o TTS estiver indisponivel. */
    public CompletableFuture<Optional<short[]>> synthesize(String text, String voice, double pitch, double speed) {
        JsonObject body = new JsonObject();
        body.addProperty("text", text);
        body.addProperty("voice", voice);
        body.addProperty("pitch", pitch);
        body.addProperty("speed", speed);
        String engine = br.com.mobvoz.config.ServerConfig.current().voiceEngine;
        if (!"auto".equals(engine)) {
            body.addProperty("engine", engine);
        }
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/synthesize"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        auth(rb);
        return http.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(r -> {
                    if (r.statusCode() != 200 || r.body().length < 2) {
                        return Optional.<short[]>empty();
                    }
                    short[] pcm = new short[r.body().length / 2];
                    ByteBuffer.wrap(r.body()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcm);
                    return Optional.of(pcm);
                })
                .exceptionally(t -> Optional.empty());
    }

    private void auth(HttpRequest.Builder rb) {
        if (token != null && !token.isEmpty()) {
            rb.header("X-MobVoz-Token", token);
        }
    }

    static Reply parse(String json) {
        try {
            JsonObject o = GSON.fromJson(json, JsonObject.class);
            List<Proposal> props = new ArrayList<>();
            JsonArray arr = o.getAsJsonArray("proposals");
            if (arr != null) {
                for (JsonElement e : arr) {
                    JsonObject po = e.getAsJsonObject();
                    Map<String, Object> params = po.has("parameters") && po.get("parameters").isJsonObject()
                            ? GSON.<Map<String, Object>>fromJson(po.get("parameters"), Map.class) : Map.of();
                    props.add(new Proposal(po.get("type").getAsString(), params));
                }
            }
            String speech = o.get("speech_text").getAsString();
            if (speech.isBlank()) {
                return Reply.unavailable();
            }
            return new Reply(speech, o.get("emotion").getAsString(), o.get("intent").getAsString(), props,
                    o.get("source").getAsString(), o.has("llm_ms") ? o.get("llm_ms").getAsLong() : 0);
        } catch (RuntimeException e) {
            return Reply.unavailable();
        }
    }
}
