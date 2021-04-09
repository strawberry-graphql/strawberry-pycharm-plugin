package rocks.strawberry

import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyTypeCheckerInspection
import kotlin.reflect.KClass


open class StrawberryTypeCheckerInspectionTest : StrawberryInspectionBase() {

    @Suppress("UNCHECKED_CAST")
    override val inspectionClass: KClass<PyInspection> = PyTypeCheckerInspection::class as KClass<PyInspection>

    fun testInit() {
        doTest()
    }
}

