package rocks.strawberry

import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.*

const val DATACLASS_SHORT_NAME = "strawberry.type"
const val DATACLASS_LONG_NAME = "strawberry.object_type.type"

val DATACLASS_NAMES = listOf(
    DATACLASS_SHORT_NAME,
    DATACLASS_LONG_NAME
)

val DATACLASS_QUALIFIED_NAME = QualifiedName.fromDottedString(DATACLASS_SHORT_NAME)
internal val PyFunction.isDataclass: Boolean get() = qualifiedName in DATACLASS_NAMES
