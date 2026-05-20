package rocks.strawberry

import org.junit.Assert.assertFalse
import org.junit.Test
import java.nio.charset.StandardCharsets

class StrawberryBinaryCompatibilityTest {

    @Test
    fun parametersProviderDoesNotLinkDirectlyToCreateEllipsis() {
        val classBytes = requireNotNull(
            StrawberryParametersProvider::class.java.classLoader.getResourceAsStream(
                "rocks/strawberry/StrawberryParametersProvider.class",
            ),
        ) { "Could not load StrawberryParametersProvider bytecode" }.use { it.readBytes() }

        val constantPoolText = String(classBytes, StandardCharsets.ISO_8859_1)

        assertFalse(
            "PyElementGenerator.createEllipsis changed its JVM return type in newer PyCharm builds",
            constantPoolText.contains("createEllipsis"),
        )
    }
}
