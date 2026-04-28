package edge;

import java.util.*;

/**
 * Switch expressions and pattern matching edge cases.
 */
public class SwitchPatternEdgeCases {

    sealed interface Expr permits Num, Add, Neg {}
    record Num(int value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Neg(Expr inner) implements Expr {}

    enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    // --- MissingEnumInSwitchCase (JAVA-E1082) ---
    // Missing CRITICAL — should fire
    String describePriority(Priority p) {
        return switch (p) {
            case LOW -> "low";
            case MEDIUM -> "medium";
            case HIGH -> "high";
            // missing CRITICAL
        };
    }

    // Exhaustive with default — should NOT fire
    String withDefault(Priority p) {
        return switch (p) {
            case LOW -> "low";
            default -> "other";
        };
    }

    // --- UnbrokenSwitchCase (JAVA-A1068) ---
    // Arrow cases — should NOT fire (arrow cases don't fall through)
    int arrowSwitch(Priority p) {
        return switch (p) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    // Colon cases without break — should fire
    int colonSwitchNoBrk(Priority p) {
        int result = 0;
        switch (p) {
            case LOW:
                result = 1;
                // missing break — should fire
            case MEDIUM:
                result = 2;
                break;
            default:
                result = 0;
        }
        return result;
    }

    // --- Pattern matching in switch (Java 21+) ---
    int eval(Expr expr) {
        return switch (expr) {
            case Num(var v) -> v;
            case Add(var l, var r) -> eval(l) + eval(r);
            case Neg(var inner) -> -eval(inner);
        };
    }

    // Guarded pattern in switch
    String classify(Object obj) {
        return switch (obj) {
            case Integer i when i > 0 -> "positive";
            case Integer i when i < 0 -> "negative";
            case Integer i -> "zero";
            case String s when s.isEmpty() -> "empty string";
            case String s -> "string: " + s;
            case null -> "null";
            default -> "other";
        };
    }

    // --- Switch with record deconstruction ---
    String describeExpr(Expr expr) {
        return switch (expr) {
            case Num(var v) when v == 0 -> "zero";
            case Num(var v) -> "number " + v;
            case Add(Num(var a), Num(var b)) -> a + " + " + b;
            case Add(var l, var r) -> "complex addition";
            case Neg(Num(var v)) -> "-" + v;
            case Neg(var inner) -> "negation";
        };
    }

    // --- Real violations inside switch ---
    void bugInSwitch(Object obj) {
        switch (obj) {
            case String s -> {
                int[] arr = new int[3];
                System.out.println(arr.toString()); // BadArrayToString — should fire
            }
            default -> {}
        }
    }
}
