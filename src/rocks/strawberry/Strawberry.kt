package rocks.strawberry

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyDecoratable
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyUnionType
import com.jetbrains.python.psi.types.TypeEvalContext

val DATACLASS_QUALIFIED_NAME = QualifiedName.fromDottedString("strawberry.type")


fun hasDecorator(pyDecoratable: PyDecoratable, refName: QualifiedName): Boolean {
    return pyDecoratable.decoratorList?.decorators?.mapNotNull { it.callee as? PyReferenceExpression }?.any {
        PyResolveUtil.resolveImportedElementQNameLocally(it).any { decoratorQualifiedName ->
            decoratorQualifiedName == refName
        }
    } ?: false
}


fun isDataclass(pyClass: PyClass): Boolean {
    return hasDecorator(pyClass, DATACLASS_QUALIFIED_NAME)
}

fun getResolveElements(referenceExpression: PyReferenceExpression, context: TypeEvalContext): Array<ResolveResult> {
    return PyResolveContext.defaultContext(context).let {
        referenceExpression.getReference(it).multiResolve(false)
    }

}

fun getResolvedPsiElements(referenceExpression: PyReferenceExpression, context: TypeEvalContext): List<PsiElement> {
    return getResolveElements(referenceExpression, context).let { PyUtil.filterTopPriorityResults(it) }
}


fun getPyClassTypeByPyTypes(pyType: PyType): List<PyClassType> {
    return when (pyType) {
        is PyUnionType -> pyType.members.mapNotNull { it }.flatMap { getPyClassTypeByPyTypes(it) }
        is PyClassType -> listOf(pyType)
        else -> listOf()
    }
}