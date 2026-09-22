package agendoc.docs;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal helpers for the table-oriented Markdown dialect used by the
 * document converters.
 */
final class MarkdownText
{

    private MarkdownText()
    {
    }

    /**
     * Escapes a value before it is placed into a Markdown table cell.
     * Backslash, pipe, and newline characters are escaped.
     *
     * @param text the raw cell text
     * @return escaped text
     */
    static String escapeCell(String text)
    {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            switch (c)
            {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '|':
                    builder.append("\\|");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * Reverts escaping applied by {@link #escapeCell(String)}.
     *
     * @param text the escaped cell text
     * @return unescaped text
     */
    static String unescapeCell(String text)
    {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length())
            {
                char next = text.charAt(i + 1);
                switch (next)
                {
                    case 'n':
                        builder.append('\n');
                        i++;
                        break;
                    case '|':
                        builder.append('|');
                        i++;
                        break;
                    case '\\':
                        builder.append('\\');
                        i++;
                        break;
                    default:
                        builder.append(c);
                }
            }
            else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * Splits a Markdown table row into cells, respecting backslash escapes.
     * Leading and trailing table pipes are ignored.
     *
     * @param line the table row line
     * @return cell values before unescaping
     */
    static List<String> splitTableRow(String line)
    {
        String trimmed = line.trim();
        int start = 0;
        int end = trimmed.length();
        if (end > 0 && trimmed.charAt(0) == '|')
        {
            start = 1;
        }
        if (end > start && trimmed.charAt(end - 1) == '|')
        {
            end--;
        }
        String core = trimmed.substring(start, end);

        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < core.length(); i++)
        {
            char c = core.charAt(i);
            if (escaped)
            {
                current.append(c);
                escaped = false;
            }
            else if (c == '\\')
            {
                current.append(c);
                escaped = true;
            }
            else if (c == '|')
            {
                cells.add(current.toString());
                current.setLength(0);
            }
            else
            {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    /**
     * Determines whether a line looks like a Markdown table row.
     *
     * @param line the line to inspect
     * @return true if the line starts with a pipe or contains an unescaped pipe
     */
    static boolean isTableRow(String line)
    {
        String trimmed = line.trim();
        return trimmed.startsWith("|") || containsUnescapedPipe(trimmed);
    }

    /**
     * Determines whether a row is a Markdown table separator row.
     *
     * @param cells already split table row cells
     * @return true if every non-empty cell consists of dashes and optional colons
     */
    static boolean isSeparatorRow(List<String> cells)
    {
        boolean hasContent = false;
        for (String cell : cells)
        {
            String trimmed = cell.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }
            hasContent = true;
            if (!trimmed.matches("^:?-{1,}:?$"))
            {
                return false;
            }
        }
        return hasContent;
    }

    private static boolean containsUnescapedPipe(String text)
    {
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (escaped)
            {
                escaped = false;
            }
            else if (c == '\\')
            {
                escaped = true;
            }
            else if (c == '|')
            {
                return true;
            }
        }
        return false;
    }
}
