import java.util.List;
import java.util.Map;

/**
 * Minimal hand-rolled JSON writer -- deliberately dependency-free (no
 * Jackson/Gson) so the whole web UI stays buildable with just `javac`, no
 * build tool or downloaded library, matching the rest of this project.
 *
 * Supports the shapes this project actually needs: String, Number, Boolean,
 * null, List, and Map (object) values, nested arbitrarily.
 */
public class JsonUtil {

    private JsonUtil() {
        // static-only holder
    }

    public static String toJson(Object value) {
        StringBuilder sb = new StringBuilder();
        write(value, sb);
        return sb.toString();
    }

    private static void write(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append('"').append(escape((String) value)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            writeMap((Map<?, ?>) value, sb);
        } else if (value instanceof Iterable) {
            writeIterable((Iterable<?>) value, sb);
        } else {
            // Fallback: treat anything unrecognized as its string form.
            sb.append('"').append(escape(value.toString())).append('"');
        }
    }

    private static void writeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(String.valueOf(entry.getKey()))).append('"').append(':');
            write(entry.getValue(), sb);
        }
        sb.append('}');
    }

    private static void writeIterable(Iterable<?> iterable, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : iterable) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            write(item, sb);
        }
        sb.append(']');
    }

    public static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}
