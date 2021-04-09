package rocks.strawberry

import com.jetbrains.python.inspections.PyInspection
import kotlin.reflect.KClass


abstract class StrawberryInspectionBase : StrawberryTestCase() {

    @Suppress("UNCHECKED_CAST")
    protected abstract val inspectionClass: KClass<PyInspection>

    private fun configureInspection() {
        myFixture!!.enableInspections(inspectionClass.java)
        myFixture!!.checkHighlighting(true, false, true)

    }

    protected fun doTest() {
        configureByFile()
        configureInspection()
    }
}
