package language.theory.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ExpressionParser {

    private static final int LEFT_ASSOC = 0;
    private static final int RIGHT_ASSOC = 1;

    private static final Map<String, int[]> OPERATORS = new HashMap<>();

    static {
        OPERATORS.put("+", new int[]{0, LEFT_ASSOC});
        OPERATORS.put("-", new int[]{0, LEFT_ASSOC});
        OPERATORS.put("*", new int[]{5, LEFT_ASSOC});
    }

    private static boolean isOperator(String token) {
        return OPERATORS.containsKey(token);
    }

    private static boolean isAssociative(String token, int type) {

        if (!isOperator(token)) {
            throw new IllegalArgumentException("Invalid token: " + token);
        }

        return OPERATORS.get(token)[1] == type;
    }

    private static int cmpPrecedence(String token1, String token2) {

        if (!isOperator(token1) || !isOperator(token2)) {
            throw new IllegalArgumentException("Invalid tokens: " + token1 + " " + token2);
        }

        return OPERATORS.get(token1)[0] - OPERATORS.get(token2)[0];
    }

    private static String[] infixToRPN(String[] inputTokens) {

        ArrayList<String> out = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String token : inputTokens) {

            if (isOperator(token)) {

                while (!stack.empty() && isOperator(stack.peek())) {

                    if ((isAssociative(token, LEFT_ASSOC) &&
                            cmpPrecedence(token, stack.peek()) <= 0) ||
                            (isAssociative(token, RIGHT_ASSOC) &&
                                    cmpPrecedence(token, stack.peek()) < 0)) {
                        out.add(stack.pop());
                        continue;
                    }
                    break;
                }

                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);  //
            } else if (token.equals(")")) {
                while (!stack.empty() && !stack.peek().equals("(")) {
                    out.add(stack.pop());
                }
                stack.pop();
            } else {
                out.add(token);
            }
        }

        while (!stack.empty()) {
            out.add(stack.pop());
        }

        String[] output = new String[out.size()];

        return out.toArray(output);
    }

    public static int compute(String[] input) {

        String[] tokens = infixToRPN(input);

        Stack<String> stack = new Stack<>();

        for (String token : tokens) {

            if (!isOperator(token)) {
                stack.push(token);
            } else {

                Integer i2 = Integer.valueOf(stack.pop());
                Integer i1 = Integer.valueOf(stack.pop());

                Integer result = token.compareTo("+") == 0 ? i1 + i2 :
                        token.compareTo("-") == 0 ? i1 - i2 :
                                i1 * i2;

                stack.push(String.valueOf(result));
            }
        }

        return Integer.parseInt(stack.pop());
    }
}