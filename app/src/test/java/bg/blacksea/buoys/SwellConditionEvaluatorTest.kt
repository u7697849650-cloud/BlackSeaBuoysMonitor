package bg.blacksea.buoys

import bg.blacksea.buoys.domain.swell.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration

class SwellConditionEvaluatorTest{
 private val evaluator=DefaultSwellConditionEvaluator()
 @Test fun low()=assertEquals(SwellConditionLevel.LOW,evaluator.evaluate(SwellConditionInput(.3,5.0,null)).level)
 @Test fun moderate()=assertEquals(SwellConditionLevel.MODERATE,evaluator.evaluate(SwellConditionInput(.7,8.0,null)).level)
 @Test fun elevated()=assertEquals(SwellConditionLevel.ELEVATED,evaluator.evaluate(SwellConditionInput(1.2,10.0,null)).level)
 @Test fun unavailable()=assertEquals(SwellConditionLevel.UNAVAILABLE,evaluator.evaluate(SwellConditionInput(null,null,null)).level)
 @Test fun missingCoastReducesConfidence()=assertEquals(AssessmentConfidence.MEDIUM,evaluator.evaluate(SwellConditionInput(.7,8.0,90.0)).confidence)
 @Test fun staleReducesConfidence()=assertEquals(AssessmentConfidence.LOW,evaluator.evaluate(SwellConditionInput(.7,8.0,90.0,coastlineOrientationDegrees=90.0,dataAge=Duration.ofHours(5))).confidence)
 @Test fun onshoreRaisesLevel()=assertEquals(SwellConditionLevel.MODERATE,evaluator.evaluate(SwellConditionInput(.3,5.0,90.0,coastlineOrientationDegrees=90.0)).level)
 @Test fun neverAboveElevated()=assertEquals(SwellConditionLevel.ELEVATED,evaluator.evaluate(SwellConditionInput(1.2,10.0,90.0,coastlineOrientationDegrees=90.0)).level)
}
