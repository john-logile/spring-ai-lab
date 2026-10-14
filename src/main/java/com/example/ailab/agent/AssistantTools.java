package com.example.ailab.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Plain Java methods the AI model can <em>call</em> as tools.
 *
 * <p>How it works: annotate a method with {@link Tool}, give it a clear
 * description, and pass this bean to {@code ChatClient.prompt().tools(...)}.
 * Spring AI converts the method into a tool definition, the model decides
 * when to call it, Spring invokes it, and the result goes back to the model.
 *
 * <p>Rules of thumb for good tools:
 * <ul>
 *   <li>One job per method, deterministic output (no LLM inside a tool).</li>
 *   <li>The {@code description} is your documentation <em>for the model</em> —
 *       write it like you'd explain the tool to a smart intern.</li>
 *   <li>Keep parameters simple types (String, int, double, enums).</li>
 * </ul>
 */
@Component
public class AssistantTools {

    @Tool(description = "Get the current date and time in a given IANA timezone, e.g. 'America/Chicago' or 'Asia/Shanghai'.")
    public String currentDateTime(
            @ToolParam(description = "IANA timezone id, e.g. America/Chicago") String timezone) {
        try {
            var zoned = ZonedDateTime.now(ZoneId.of(timezone));
            return zoned.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a z"));
        } catch (Exception e) {
            return "Unknown timezone: " + timezone + ". Use an IANA id like America/Chicago.";
        }
    }

    @Tool(description = "Evaluate a simple arithmetic expression with + - * / and parentheses, e.g. '15% of 240' is not supported, use '(15/100)*240'.")
    public String calculate(
            @ToolParam(description = "Arithmetic expression, e.g. '(15/100)*240' or '7*8+2'") String expression) {
        try {
            double result = new ExpressionParser(expression).parse();
            // Print whole numbers without decimals
            if (result == Math.rint(result)) {
                return String.valueOf((long) result);
            }
            return String.valueOf(result);
        } catch (Exception e) {
            return "Could not evaluate expression: " + expression;
        }
    }

    @Tool(description = "Convert an amount between USD, EUR, CNY and JPY using fixed demo rates.")
    public String convertCurrency(
            @ToolParam(description = "Amount to convert, e.g. 100") double amount,
            @ToolParam(description = "Source currency code: USD, EUR, CNY or JPY") String from,
            @ToolParam(description = "Target currency code: USD, EUR, CNY or JPY") String to) {
        // Demo rates relative to USD (hard-coded on purpose: tools should be deterministic)
        var rates = java.util.Map.of("USD", 1.0, "EUR", 0.92, "CNY", 7.24, "JPY", 149.8);
        Double fromRate = rates.get(from.toUpperCase());
        Double toRate = rates.get(to.toUpperCase());
        if (fromRate == null || toRate == null) {
            return "Supported currencies: USD, EUR, CNY, JPY.";
        }
        double usd = amount / fromRate;
        double converted = usd * toRate;
        return String.format("%.2f %s = %.2f %s (demo rates)", amount, from.toUpperCase(), converted, to.toUpperCase());
    }

    /**
     * Tiny recursive-descent parser for + - * / and parentheses.
     * Kept inline so the lab has zero extra dependencies.
     */
    static class ExpressionParser {
        private final String input;
        private int pos = 0;

        ExpressionParser(String input) {
            this.input = input.replaceAll("\\s+", "");
        }

        double parse() {
            double value = parseAddSub();
            if (pos != input.length()) {
                throw new IllegalArgumentException("Unexpected character at position " + pos);
            }
            return value;
        }

        private double parseAddSub() {
            double value = parseMulDiv();
            while (pos < input.length()) {
                char op = input.charAt(pos);
                if (op == '+' || op == '-') {
                    pos++;
                    double rhs = parseMulDiv();
                    value = (op == '+') ? value + rhs : value - rhs;
                } else {
                    break;
                }
            }
            return value;
        }

        private double parseMulDiv() {
            double value = parseFactor();
            while (pos < input.length()) {
                char op = input.charAt(pos);
                if (op == '*' || op == '/') {
                    pos++;
                    double rhs = parseFactor();
                    value = (op == '*') ? value * rhs : value / rhs;
                } else {
                    break;
                }
            }
            return value;
        }

        private double parseFactor() {
            if (pos < input.length() && input.charAt(pos) == '(') {
                pos++;
                double value = parseAddSub();
                if (pos >= input.length() || input.charAt(pos) != ')') {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                }
                pos++;
                return value;
            }
            int start = pos;
            while (pos < input.length()
                    && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            if (start == pos) {
                throw new IllegalArgumentException("Expected number at position " + pos);
            }
            return Double.parseDouble(input.substring(start, pos));
        }
    }
}
