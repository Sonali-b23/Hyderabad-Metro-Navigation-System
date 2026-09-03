import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dependency-free tests for JsonUtil, following the same style as
 * GraphMTest.java (no JUnit -- just javac + java).
 */
public class JsonUtilTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testEscapesQuotesAndBackslashes();
        testEscapesControlCharacters();
        testSerializesPrimitives();
        testSerializesList();
        testSerializesNestedMap();
        testSerializesNull();

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testEscapesQuotesAndBackslashes() {
        check("escapes quotes and backslashes",
                JsonUtil.escape("say \"hi\" \\ ok").equals("say \\\"hi\\\" \\\\ ok"));
    }

    private static void testEscapesControlCharacters() {
        check("escapes newline/tab", JsonUtil.escape("a\nb\tc").equals("a\\nb\\tc"));
    }

    private static void testSerializesPrimitives() {
        check("serializes string", JsonUtil.toJson("hello").equals("\"hello\""));
        check("serializes number", JsonUtil.toJson(13.5).equals("13.5"));
        check("serializes boolean", JsonUtil.toJson(true).equals("true"));
        check("serializes null", JsonUtil.toJson(null).equals("null"));
    }

    private static void testSerializesList() {
        String json = JsonUtil.toJson(List.of("Raidurg", "HITEC City"));
        check("serializes list", json.equals("[\"Raidurg\",\"HITEC City\"]"));
    }

    private static void testSerializesNull() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", null);
        check("serializes null field in map", JsonUtil.toJson(map).equals("{\"path\":null}"));
    }

    private static void testSerializesNestedMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("found", true);
        map.put("totalCost", 13.5);
        map.put("path", List.of("A", "B"));

        String json = JsonUtil.toJson(map);
        check("serializes nested map in insertion order",
                json.equals("{\"found\":true,\"totalCost\":13.5,\"path\":[\"A\",\"B\"]}"));
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + description);
        } else {
            failed++;
            System.out.println("  [FAIL] " + description);
        }
    }
}
