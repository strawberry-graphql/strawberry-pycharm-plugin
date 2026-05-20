package rocks.strawberry

import com.intellij.testFramework.UsefulTestCase
import org.junit.Assert.assertTrue
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.charset.StandardCharsets.UTF_8

class StrawberryBinaryCompatibilityTest : UsefulTestCase() {

    fun testParametersProviderDoesNotLinkDirectlyToCreateEllipsis() {
        val classBytes = requireNotNull(
            StrawberryParametersProvider::class.java.classLoader.getResourceAsStream(
                "rocks/strawberry/StrawberryParametersProvider.class",
            ),
        ) { "Could not load StrawberryParametersProvider bytecode" }.use { it.readBytes() }

        val createEllipsisReferences = methodReferences(classBytes).filter {
            it.owner == "com/jetbrains/python/psi/PyElementGenerator" && it.name == "createEllipsis"
        }

        assertTrue(
            "PyElementGenerator.createEllipsis changed its JVM return type in newer PyCharm builds: $createEllipsisReferences",
            createEllipsisReferences.isEmpty(),
        )
    }

    private data class MethodReference(val owner: String, val name: String, val descriptor: String)

    private fun methodReferences(classBytes: ByteArray): List<MethodReference> {
        val input = DataInputStream(ByteArrayInputStream(classBytes))

        require(input.readInt() == 0xCAFEBABE.toInt()) { "Expected a JVM class file" }
        input.skipBytes(4)

        val constantPoolCount = input.readUnsignedShort()
        val utf8Entries = arrayOfNulls<String>(constantPoolCount)
        val classNameIndexes = IntArray(constantPoolCount)
        val nameAndTypeNameIndexes = IntArray(constantPoolCount)
        val nameAndTypeDescriptorIndexes = IntArray(constantPoolCount)
        val methodIndexes = mutableListOf<Pair<Int, Int>>()

        var index = 1
        while (index < constantPoolCount) {
            when (val tag = input.readUnsignedByte()) {
                1 -> {
                    val length = input.readUnsignedShort()
                    val bytes = ByteArray(length)
                    input.readFully(bytes)
                    utf8Entries[index] = String(bytes, UTF_8)
                }
                3, 4, 17, 18 -> input.skipBytes(4)
                5, 6 -> {
                    input.skipBytes(8)
                    index++
                }
                7 -> classNameIndexes[index] = input.readUnsignedShort()
                8, 16, 19, 20 -> input.skipBytes(2)
                9 -> input.skipBytes(4)
                10, 11 -> methodIndexes.add(input.readUnsignedShort() to input.readUnsignedShort())
                12 -> {
                    nameAndTypeNameIndexes[index] = input.readUnsignedShort()
                    nameAndTypeDescriptorIndexes[index] = input.readUnsignedShort()
                }
                15 -> input.skipBytes(3)
                else -> error("Unsupported constant pool tag $tag")
            }
            index++
        }

        return methodIndexes.map { (classIndex, nameAndTypeIndex) ->
            MethodReference(
                owner = utf8Entries[classNameIndexes[classIndex]].orEmpty(),
                name = utf8Entries[nameAndTypeNameIndexes[nameAndTypeIndex]].orEmpty(),
                descriptor = utf8Entries[nameAndTypeDescriptorIndexes[nameAndTypeIndex]].orEmpty(),
            )
        }
    }
}
