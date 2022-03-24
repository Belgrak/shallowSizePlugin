package spbu.kotlin.shallow.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

const val DEFAULT_SIZE = 8

class AddShallowSizeMethodTest {
    @ParameterizedTest(name = "case [{index}]: {0}")
    @MethodSource("addTestData")
    fun shallowSizeTest(testClass: Any, result: Int) {
        require(testClass::class.isData) { "testClass should be data class" }

        testClass::class.members.find { it.name == "shallowSize" }?.let {
            assertEquals(result, it.call(testClass))
        }
    }

    private companion object {
        @JvmStatic
        fun addTestData() = listOf(
            Arguments.of(BaseClass("Hello"), DEFAULT_SIZE),
            Arguments.of(InternalClass(true), 1),
            Arguments.of(InheritInterfaces(3), Int.SIZE_BYTES),
            Arguments.of(InheritClass(3), Int.SIZE_BYTES),
            Arguments.of(NoBackField('c'), 2),
            Arguments.of(PrivateFields(3), Long.SIZE_BYTES + Int.SIZE_BYTES),
            Arguments.of(
                MultipleFieldsInConstructor(1, 2, 3, 4),
                Byte.SIZE_BYTES + Short.SIZE_BYTES + Int.SIZE_BYTES + Long.SIZE_BYTES
            ),
            Arguments.of(
                NullablePrimitives(1f, 1.0, 'c', true),
                4 * DEFAULT_SIZE
            ),
            Arguments.of(JavaCharacter(Character('3')), DEFAULT_SIZE),
            Arguments.of(NoExplicitType(3), Long.SIZE_BYTES + Int.SIZE_BYTES),
            Arguments.of(OverrideFieldFromClass(4), Int.SIZE_BYTES),
            Arguments.of(OverrideFieldFromInterface(4), Int.SIZE_BYTES),
        )
    }
}