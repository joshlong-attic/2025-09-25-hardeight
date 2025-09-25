package com.example.kotlinftw

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinftwApplication

fun main(args: Array<String>) {
    runApplication<KotlinftwApplication>(*args)
}

open class A {
}

class B : A() {
}


fun foo() {

    val func: (Int) -> String = { p -> p.toString() }
    func(2)

    var name0 = ""
    name0 = ""

    val name1 = "josh"
    // name1 = ""
}




