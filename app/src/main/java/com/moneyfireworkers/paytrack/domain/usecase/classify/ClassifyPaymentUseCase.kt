package com.moneyfireworkers.paytrack.domain.usecase.classify

import com.moneyfireworkers.paytrack.classifier.engine.RuleBasedClassifier
import com.moneyfireworkers.paytrack.domain.model.ClassificationDecision
import com.moneyfireworkers.paytrack.domain.model.ClassificationRule
import com.moneyfireworkers.paytrack.domain.model.ParsedPayment

class ClassifyPaymentUseCase(
    private val classifier: RuleBasedClassifier = RuleBasedClassifier(),
) {
    operator fun invoke(parsedPayment: ParsedPayment, rules: List<ClassificationRule>): ClassificationDecision {
        return classifier.classify(parsedPayment, rules)
    }
}
