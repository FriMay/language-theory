package test;

import language.theory.liksin.biriukov.*;
import org.junit.Test;

import java.util.List;

import static language.theory.liksin.biriukov.GrammarParser.PARSER_SYMBOL;


public class GrammarParserTest {

    @Test
    public void shouldCreateGrammar() {

        String stringGrammar =
                String.format("%s%s%s",
                        "Vt = { " +
                                    "var" + PARSER_SYMBOL +
                                    "integer" + PARSER_SYMBOL +
                                    "begin" + PARSER_SYMBOL +
                                    "end" + PARSER_SYMBOL +
                                    "write" + PARSER_SYMBOL +
                                    "read" + PARSER_SYMBOL +
                                    "for" + PARSER_SYMBOL +
                                    "to" + PARSER_SYMBOL +
                                    "do" + PARSER_SYMBOL +
                                    "end_for" + PARSER_SYMBOL +
                                    "," + PARSER_SYMBOL +
                                    ":" + PARSER_SYMBOL +
                                    ";" + PARSER_SYMBOL +
                                    "(" + PARSER_SYMBOL +
                                    ")" + PARSER_SYMBOL +
                                    "=" + PARSER_SYMBOL +
                                    "+" + PARSER_SYMBOL +
                                    "-" + PARSER_SYMBOL +
                                    "*" + PARSER_SYMBOL +
                                    "0-9" + PARSER_SYMBOL +
                                    "a-z" + PARSER_SYMBOL +
                                "} ",
                        "Va = { " +
                                "<Программа>" + PARSER_SYMBOL +
                                "<ОписаниеВычислений>" + PARSER_SYMBOL +
                                "<ОписаниеПеременных>" + PARSER_SYMBOL +
                                "<СписокПеременных>" + PARSER_SYMBOL +
                                "<СписокПеременных'>" + PARSER_SYMBOL +
                                "<СписокПрисваиваний>" + PARSER_SYMBOL +
                                "<СписокПрисваиваний'>" + PARSER_SYMBOL +
                                "<Присваивание>" + PARSER_SYMBOL +
                                "<Выражение>" + PARSER_SYMBOL +
                                "<Присваивание>" + PARSER_SYMBOL +
                                "<Подвыражение>" + PARSER_SYMBOL +
                                "<Подвыражение'>" + PARSER_SYMBOL +
                                "<Операнд>" + PARSER_SYMBOL +
                                "<Идентификатор>" + PARSER_SYMBOL +
                                "<Идентификатор'>" + PARSER_SYMBOL +
                                "<Константа>" + PARSER_SYMBOL +
                                "<Буква>" + PARSER_SYMBOL +
                                "<УнарныйОператор>" + PARSER_SYMBOL +
                                "<БинарныйОператор>" + PARSER_SYMBOL +
                                "<СписокДействий>" + PARSER_SYMBOL +
                                "<СписокДействий'>" + PARSER_SYMBOL +
                                " } ",
                        "R = {" +
                                    "<Программа> -> <ОписаниеПеременных> / <ОписаниеВычислений>" + PARSER_SYMBOL +
                                    "<ОписаниеВычислений> -> begin / <СписокПрисваиваний>" + PARSER_SYMBOL +
                                    "<ОписаниеПеременных> -> var / <СписокПеременных> / integer / ;"  + PARSER_SYMBOL +
                                    "<СписокПеременных> -> <Идентификатор> / <СписокПеременных'>" + PARSER_SYMBOL +
                                    "<СписокПеременных'> -> , / <СписокПеременных> | : |" + PARSER_SYMBOL +
                                    "<СписокПрисваиваний> -> <Присваивание> / <СписокПрисваиваний'> | end" + PARSER_SYMBOL +
                                    "<СписокПрисваиваний'> -> <СписокПрисваиваний> |" + PARSER_SYMBOL +
                                    "<Присваивание> -> <Идентификатор> / = / <Выражение> / ;" + PARSER_SYMBOL +
                                    "<Выражение> -> <УнарныйОператор> / <Подвыражение> | <Подвыражение>" + PARSER_SYMBOL +
                                    "<Подвыражение> -> ( / <Выражение> / ) / <Подвыражение'> " +
                                            "| <Операнд> / <Подвыражение'>" + PARSER_SYMBOL +
                                    "<Подвыражение'> -> <БинарныйОператор> / <Подвыражение> |" + PARSER_SYMBOL +
                                    "<Операнд> -> <Идентификатор> " +
                                            "| <Константа>" + PARSER_SYMBOL +
                                    "<Идентификатор> -> <Буква> / <Идентификатор'>" + PARSER_SYMBOL +
                                    "<Идентификатор'> -> <Идентификатор> |" + PARSER_SYMBOL +
                                    "<Константа> -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9" + PARSER_SYMBOL +
                                    "<Буква> -> a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z" + PARSER_SYMBOL +
                                    "<УнарныйОператор> -> -" + PARSER_SYMBOL +
                                    "<БинарныйОператор> -> + | - | *" + PARSER_SYMBOL +
                                    "<СписокДействий> -> read / ( / <Идентификатор> / ) / <СписокДействий'> " +
                                            "| write / ( / <Идентификатор> / ) / <СписокДействий'> " +
                                            "| for / ( / <Присваивание> / ) / to / ( / <Выражение> / ) / do / <СписокДействий'> / end_for / ;" + PARSER_SYMBOL +
                                    "<СписокДействий'> -> <СписокДействий> | <СписокПрисваиваний> |" +
                                "}"
                );

        Grammar grammar = GrammarParser.parseGrammar(stringGrammar);

        List<ReplaceFunction> replaceFunctionList
                = ReplaceFunctionGenerator.generateReplaceFunctions(grammar);

        String testProgram = "var aa, b: integer;" +
                "begin" +
                "aa = -4*2;" +
                "b = 7;" +
                "end";

        ReplaceFunctionApplier.apply(testProgram,"<Программа>", replaceFunctionList, grammar);

        System.out.println(grammar);
    }
}
