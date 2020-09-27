package language.theory.liksin.biriukov;

import java.util.List;
import java.util.Stack;

public class ReplaceFunctionApplier {

    private static final Stack<String> stack = new Stack<>();

    private static String currentOrder;

    public static void apply(String startOrder, String startRule, List<ReplaceFunction> replaceFunctionList) {

        currentOrder = startOrder;

        stack.push(startRule);

        while (currentOrder.length() != 0 && !stack.isEmpty()) {

            replaceFunctionList.forEach(it -> {

                if (currentOrder.startsWith(it.getLeftReadOrder()) && stack.peek().equals(it.getRightReadOrder())) {

                    if (it.getIsRead()) {
                        setCurrentOrder(currentOrder, it.getLeftReadOrder().length());
                    }

                    stack.pop();

                    it.getWriteOrder().forEach(stack::push);
                }
            });
        }

        System.out.println("Success");
    }

    private static void setCurrentOrder(String order, Integer size) {

        StringBuilder newOrder = new StringBuilder();

        for (int i = size; i < order.length(); ++i) {
            newOrder.append(order.charAt(i));
        }

        currentOrder = newOrder.toString();
    }
}
