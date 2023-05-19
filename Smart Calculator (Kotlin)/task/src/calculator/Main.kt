package calculator

import kotlin.math.pow
import java.math.BigInteger

fun main() {
    var variables = mutableMapOf<String, String>()
    while (true) {
        val input = readln()
        when {
            input == "/exit" -> {
                println("Bye!")
                break
            }
            input == "/help" -> println("The program calculates the sum, dif, mul, div of numbers.\n" +
                    "Also it supports variables and parentheses")
            input.isEmpty() -> continue
            Regex("/\\w+").matches(input) -> println("Unknown command")
            input.split("").filter { it == "(" }.size != input.split("").filter { it == ")" }.size ->
                println("Invalid expression")
            input.contains(Regex("[*][*]+")) || input.contains(Regex("//+")) -> println("Invalid expression")
            input.contains("=") -> if (checkAssignment(input, variables)) variables = assignment(input, variables)
            variables.keys.contains(input.trim()) -> println(variables[input.trim()])
            variables.keys.map { it.lowercase() }.contains(input.lowercase().trim()) -> println("Unknown variable")
            else -> try {
                println(calculatePostfixNotation(makePostFixNotation(input), variables))
            } catch (e:Exception) {
                println("Invalid expression")
            }
        }
    }
    var list = "little brown fox brown fog".split(" ")
    println(list.count { it == "little" })
}

fun makePostFixNotation(input: String): List<String> {
    val numbersAndVariables = input.replace(Regex("\\W"), " ").split(Regex("\\s+")).iterator()
    val result = mutableListOf<String>()
    val stack = mutableListOf<Char>()
    var i = 0
    do {
        if (input[i].toString().contains(Regex("\\W"))) {
            when {
                input[i].toString().isBlank() -> i++
                input[i] == '(' -> {
                    stack.add(input[i])
                    i++
                }
                input[i] == ')' -> {
                    while (stack.last() != '(') {
                        result += stack.last().toString()
                        stack.removeAt(stack.lastIndex)
                    }
                    stack.removeAt(stack.lastIndex)
                    i++
                }
                stack.isEmpty() || stack.last() == '(' -> {
                    var j = i
                    while (input[j] == input[j + 1]) j++
                    j++
                    when (input[i]) {
                        '-' -> if ((j - i) % 2 != 0) stack.add('-') else stack.add('+')
                        else -> stack.add(input[i])
                    }
                    i = j
                }
                input[i].toString().contains(Regex("[+-]")) -> {
                    var j = i
                    while (input[j] == input[j + 1]) j++
                    j++
                    while (stack.last() != '(' || stack.last().toString().contains(Regex("[+-]"))) {
                        result.add(stack.removeLast().toString())
                        if (stack.isEmpty()) break
                    }
                    when (input[i]) {
                        '+' -> stack.add(input[i])
                        '-' -> if ((j - i) % 2 != 0) stack.add('-') else stack.add('+')
                    }
                    i = j
                }
                input[i].toString().contains(Regex("[*/]")) -> {
                    when {
                        stack.last().toString().contains(Regex("[*/^]")) -> {
                            while (stack.last().toString().contains(Regex("[*/(+-]"))) {
                                result.add(stack.removeLast().toString())
                                if (stack.isEmpty()) break
                            }
                            stack.add(input[i])
                        }
                        else -> stack.add(input[i])
                    }
                    i++
                }
                input[i] == '^' -> {
                    when {
                        stack.last() == '^' -> {
                            while (stack.last() != '(') {
                                result.add(stack.removeLast().toString())
                                if (stack.isEmpty()) break
                            }
                            stack.add(input[i])
                        }
                        else -> stack.add(input[i])
                    }
                    i++
                }
            }
        } else {
            result += numbersAndVariables.next()
            i += result.last().length
        }
    } while (i != input.length )
    while (stack.isNotEmpty()) {
        result += stack.last().toString()
        stack.removeAt(stack.lastIndex)
    }
    return result.toList()
}

fun calculatePostfixNotation(postfixNotation: List<String>, variables: MutableMap<String, String>): BigInteger {
    var firstOperand: BigInteger
    var secondOperand: BigInteger
    val stack = mutableListOf<BigInteger>()
    for (string in postfixNotation) {
        when {
            string.contains(Regex("[a-zA-Z]+")) -> stack.add(variables[string]!!.toBigInteger())
            string.contains(Regex("\\d+")) -> stack.add(string.toBigInteger())
            else -> {
                secondOperand = stack.removeLast()
                firstOperand = stack.removeLast()
                when (string) {
                    "+" -> stack.add(firstOperand + secondOperand)
                    "-" -> stack.add(firstOperand - secondOperand)
                    "*" -> stack.add(firstOperand * secondOperand)
                    "/" -> stack.add(firstOperand / secondOperand)
                    "^" -> stack.add(firstOperand.pow(secondOperand.toInt()))
                }
            }
        }
    }
    return stack.last()
}

fun checkAssignment(input: String, variables: MutableMap<String,String>): Boolean {
    val buffer = input.split(Regex("=")).map { it.trim() }
    var result = false
    when {
        buffer[0].contains(Regex("\\d+")) || !buffer[0].contains(Regex("[a-zA-Z]+")) -> println("Invalid identifier")
        input.split(Regex("\\s+")).filter { it == "=" }.size > 1 -> println("Invalid assignment")
        buffer[1].contains(Regex("[a-zA-Z]+\\d+")) || buffer[1].contains(Regex("\\d+[a-zA-Z]+")) -> println("Invalid identifier")
        else -> {
            try {
                buffer[1].toBigInteger()
                result = true
            } catch (e: Exception) {
                if (!buffer[1].contains(Regex("\\d+")) && variables.keys.contains(buffer[1])) result = true
                else println("Invalid identifier")
            }
        }
    }
    return result
}

fun assignment(input: String, variables: MutableMap<String,String>): MutableMap<String,String> {
    val buffer = input.split(Regex("=")).map { it.trim() }
    val variables = variables
    try {
        buffer[1].toBigInteger()
        variables[buffer[0]] = buffer[1]
    } catch (e: Exception) {
        variables[buffer[0]] = variables[buffer[1]].toString()
    }
    return variables
}