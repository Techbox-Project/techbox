import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class Test : StringSpec({
    "1 + 1 should be 2" {
        (1 + 1) shouldBe 2
    }
})