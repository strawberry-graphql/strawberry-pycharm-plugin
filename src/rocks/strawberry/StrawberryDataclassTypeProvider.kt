package rocks.strawberry

import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.*

class StrawberryDataclassTypeProvider : PyTypeProviderBase() {
    override fun getCallableType(callable: PyCallable, context: TypeEvalContext): PyType? {
        if (callable is PyFunction && callable.isDataclass) {
            // Drop fake dataclass return type
            return PyCallableTypeImpl(callable.getParameters(context), null)
        }
        return super.getCallableType(callable, context)
    }
}
