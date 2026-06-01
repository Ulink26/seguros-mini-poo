package seguros.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Codifica filas legibles y permite usar |, saltos de linea y diagonales
 * dentro de los valores sin romper los archivos de texto.
 */
final class TextCodec {
    private TextCodec() {
    }

    static String row(Object... values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                result.append('|');
            }
            result.append(escape(String.valueOf(values[i])));
        }
        return result.toString();
    }

    static List<String> split(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaped) {
                if (character == 'n') {
                    current.append('\n');
                } else if (character == 'r') {
                    current.append('\r');
                } else if (character == 'p') {
                    current.append('|');
                } else {
                    current.append(character);
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '|') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (escaped) {
            current.append('\\');
        }
        values.add(current.toString());
        return values;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("|", "\\p")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
