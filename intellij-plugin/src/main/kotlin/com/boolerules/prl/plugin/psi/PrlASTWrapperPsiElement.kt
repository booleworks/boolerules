package com.boolerules.prl.plugin.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * **This class is a hack.**
 *
 * For some reason, the grammar kit adds `@Override` annotations to the [accept] methods of [ASTWrapperPsiElement]s,
 * although no such method exists. (In fact this happens only on the second run of the grammar kit -- but we have another
 * problem that the methods from [PrlPsiImplUtil] are only recognized/generated on the *second* run of the grammar
 * kit)
 *
 * So as a workaround, the respective method is implemented here s.t. the `@Override` annotation in the generated code is valid.
 */
abstract class PrlASTWrapperPsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {

    abstract fun accept(visitor: Visitor)
}
