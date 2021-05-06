package rocks.strawberry

import com.intellij.psi.PsiElement
import com.jetbrains.python.codeInsight.stdlib.PyDataclassTypeProvider
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyCallExpressionImpl
import com.jetbrains.python.psi.impl.PyCallExpressionNavigator
import com.jetbrains.python.psi.types.*

class StrawberryDataclassTypeProvider : PyTypeProviderBase() {
    private val pyDataclassTypeProvider = PyDataclassTypeProvider()

    override fun getReferenceExpressionType(
        referenceExpression: PyReferenceExpression,
        context: TypeEvalContext
    ): PyType? {
        return getDataclass(referenceExpression, context)
    }


    private fun getDataclassCallableType(
        referenceTarget: PsiElement,
        context: TypeEvalContext,
        callSite: PyCallExpression?
    ): PyCallableType? {
        return pyDataclassTypeProvider.getReferenceType(
            referenceTarget,
            context,
            callSite ?: PyCallExpressionImpl(referenceTarget.node)
        )?.get() as? PyCallableType
    }

    private fun getDataclassType(
        referenceTarget: PsiElement,
        context: TypeEvalContext,
        pyReferenceExpression: PyReferenceExpression,
        definition: Boolean
    ): PyType? {
        val callSite = PyCallExpressionNavigator.getPyCallExpressionByCallee(pyReferenceExpression)
        val dataclassCallableType = getDataclassCallableType(referenceTarget, context, callSite) ?: return null
        val dataclassType = (dataclassCallableType).getReturnType(context) as? PyClassType ?: return null
        if (!isDataclass(dataclassType.pyClass)) return null

        return when {
            callSite is PyCallExpression && definition -> dataclassCallableType
            definition -> dataclassType.toClass()
            else -> dataclassType
        }
    }


    private fun getDataclass(referenceExpression: PyReferenceExpression, context: TypeEvalContext): PyType? {
        return getResolvedPsiElements(referenceExpression, context)
            .asSequence()
            .mapNotNull {
                when {
                    it is PyClass && isDataclass(it) ->
                        getDataclassType(it, context, referenceExpression, true)
                    it is PyTargetExpression -> (it as? PyTypedElement)
                        ?.let { pyTypedElement -> context.getType(pyTypedElement) }
                        ?.let { pyType -> getPyClassTypeByPyTypes(pyType) }
                        ?.filter { pyClassType -> isDataclass(pyClassType.pyClass) }
                        ?.mapNotNull { pyClassType ->
                            getDataclassType(pyClassType.pyClass,
                                context,
                                referenceExpression,
                                pyClassType.isDefinition)
                        }
                        ?.firstOrNull()
                    else -> null
                }
            }.firstOrNull()
    }
}
