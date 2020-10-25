package language.theory.interpreter;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class Compiler {

    private static final Stack<String> stack = new Stack<>();

    private static String currentOrder;

    public static boolean compile(String startOrder, String startRule, List<ReplaceFunction> replaceFunctionList) {

        currentOrder = startOrder.toLowerCase();

        stack.push(startRule);

        printCurrentCondition();

        while (currentOrder.length() != 0 && !stack.isEmpty()) {

            AtomicBoolean isRuleApplied = new AtomicBoolean(false);

            replaceFunctionList.forEach(it -> {

                if (currentOrder.startsWith(it.getLeftReadOrder()) && stack.peek().equals(it.getRightReadOrder())) {

                    if (it.getIsRead()) {
                        setCurrentOrder(currentOrder, it.getLeftReadOrder().length());
                    }

                    stack.pop();

                    it.getWriteOrder().forEach(stack::push);

                    System.out.print("\n -> ");

                    printCurrentCondition();

                    isRuleApplied.set(true);
                }
            });

            if (!isRuleApplied.get()) {
                System.out.printf("\n\nFor rule %s not found function with consume parameter \"%s\"", stack.peek(), currentOrder.charAt(0));
                return false;
            }
        }

        System.out.println();

        return true;
    }

    private static void setCurrentOrder(String order, Integer size) {

        StringBuilder newOrder = new StringBuilder();

        for (int i = size; i < order.length(); ++i) {
            newOrder.append(order.charAt(i));
        }

        currentOrder = newOrder.toString();
    }

    private static void printCurrentCondition() {
        System.out.printf("(S0, %s, h0%s)", currentOrder, String.join("", stack));
    }
}
