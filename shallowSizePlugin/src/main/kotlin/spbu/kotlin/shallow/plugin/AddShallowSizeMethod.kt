package spbu.kotlin.shallow.plugin

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.functions

const val DEFAULT_SIZE = 8

fun IrType.byteSize(): Int {
    return when {
        isChar() -> 2
        isByte() || isUByte() -> 1
        isShort() || isUShort() -> 2
        isInt() -> 4
        isLong() || isULong() -> 8
        isFloat() -> 4
        isDouble() -> 8
        isBoolean() -> 1
        isUnit() -> 4
        else -> DEFAULT_SIZE
    }
}

val Meta.GenerateShallowSize: CliPlugin
    get() = "Generate shallowSize method" {
        meta(
            classDeclaration(this, { element.isData() }) { declaration ->
                Transform.replace(
                    replacing = declaration.element,
                    newDeclaration = """
                            |$`@annotations` $kind $name $`(typeParameters)` $`(params)` : $superTypes"{
                            |   $body
                            |   fun void shallowSize(): Int 
                            | } """.`class`.syntheticScope
                )
            },
            irClass { clazz ->
                if (clazz.isData) {
                    var sumOfSized = 0
                    for (element in clazz.superTypes) {
                        sumOfSized += element.byteSize()
                    }
                    clazz.functions.find { it.name.toString() == "shallowSize" }?.let { shallowSize ->
                        shallowSize.body = DeclarationIrBuilder(pluginContext, shallowSize.symbol).irBlockBody {
                            +irReturn(irInt(sumOfSized))
                        }
                    }
                }
                clazz
            }
        )
    }
