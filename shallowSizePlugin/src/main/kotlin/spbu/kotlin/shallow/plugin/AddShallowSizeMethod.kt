package spbu.kotlin.shallow.plugin

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.backend.common.ir.allParametersCount
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isUByte
import org.jetbrains.kotlin.ir.types.isShort
import org.jetbrains.kotlin.ir.types.isUShort
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isULong
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.properties

const val DEFAULT_SIZE = 8
const val BOOLEAN_SIZE = 4
const val UNIT_SIZE = 8

fun IrType.byteSize(): Int {
    return when {
        isChar() -> Char.SIZE_BYTES
        isByte() || isUByte() -> Byte.SIZE_BYTES
        isShort() || isUShort() -> Short.SIZE_BYTES
        isInt() -> Int.SIZE_BYTES
        isLong() || isULong() -> Long.SIZE_BYTES
        isFloat() -> Float.SIZE_BYTES
        isDouble() -> Double.SIZE_BYTES
        isBoolean() -> BOOLEAN_SIZE
        isUnit() -> UNIT_SIZE
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
                            |$`@annotations` $kind $name $`(typeParameters)` $`(params)` $superTypes {
                            |   $body
                            |   fun shallowSize(): Int = 0
                            | } """.`class`
                )
            },
            irClass { clazz ->
                if (clazz.isData) {
                    var sumOfSized = 0
                    for (element in clazz.properties) {
                        sumOfSized += element.backingField?.type?.byteSize() ?: 0
                    }
                    clazz.functions.find { it.name.toString() == "shallowSize" && it.allParametersCount == 0 }
                        ?.let { shallowSize ->
                            shallowSize.body = DeclarationIrBuilder(pluginContext, shallowSize.symbol).irBlockBody {
                                +irReturn(irInt(sumOfSized))
                            }
                        } ?: throw NoSuchMethodError("shallowSize function not found")
                }
                clazz
            }
        )
    }
