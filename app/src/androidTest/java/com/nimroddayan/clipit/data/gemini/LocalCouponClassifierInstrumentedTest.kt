package com.nimroddayan.clipit.data.gemini

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [LocalCouponClassifier]. Tests run on an Android device/emulator since the
 * classifier uses Android Context.
 */
@RunWith(AndroidJUnit4::class)
class LocalCouponClassifierInstrumentedTest {

    private lateinit var classifier: LocalCouponClassifier

    @Before
    fun setUp() {
        classifier = LocalCouponClassifier(ApplicationProvider.getApplicationContext())
    }

    // ========== English Keyword Tests ==========

    @Test
    fun isCoupon_returnsTrue_forTextContainingCoupon() {
        assertTrue(classifier.isCoupon("Get your coupon today!"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingCode() {
        assertTrue(classifier.isCoupon("Use code SAVE20 at checkout"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingVoucher() {
        assertTrue(classifier.isCoupon("Your voucher is ready"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingPromo() {
        assertTrue(classifier.isCoupon("Promo offer available"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingDiscount() {
        assertTrue(classifier.isCoupon("Get 50% Discount today"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingSave() {
        assertTrue(classifier.isCoupon("Save big on your purchase"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingOff() {
        assertTrue(classifier.isCoupon("20% Off everything"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingGiftCard() {
        assertTrue(classifier.isCoupon("This is a gift card worth 100"))
    }

    @Test
    fun isCoupon_returnsTrue_forTextContainingGift() {
        assertTrue(classifier.isCoupon("Here is your Gift"))
    }

    // ========== Hebrew Keyword Tests ==========

    @Test
    fun isCoupon_returnsTrue_forHebrewCoupon() {
        assertTrue(classifier.isCoupon("קופון הנחה בשווי 50 שקל"))
    }

    @Test
    fun isCoupon_returnsTrue_forHebrewCode() {
        assertTrue(classifier.isCoupon("הקוד שלך הוא 1234"))
    }

    @Test
    fun isCoupon_returnsTrue_forHebrewVoucher() {
        assertTrue(classifier.isCoupon("שובר מתנה לרכישה"))
    }

    @Test
    fun isCoupon_returnsTrue_forHebrewGift() {
        assertTrue(classifier.isCoupon("כרטיס מתנה בשבילך"))
    }

    @Test
    fun isCoupon_returnsTrue_forHebrewBenefit() {
        assertTrue(classifier.isCoupon("הטבה מיוחדת"))
    }

    @Test
    fun isCoupon_returnsTrue_forHebrewRedemption() {
        assertTrue(classifier.isCoupon("ניתן למימוש בחנות"))
    }

    // ========== Currency Pattern Tests ==========

    @Test
    fun isCoupon_returnsTrue_forShekelSymbolBeforeNumber() {
        assertTrue(classifier.isCoupon("This card is worth ₪100"))
    }

    @Test
    fun isCoupon_returnsTrue_forShekelSymbolAfterNumber() {
        assertTrue(classifier.isCoupon("This card is worth 100₪"))
    }

    @Test
    fun isCoupon_returnsTrue_forDollarSymbol() {
        assertTrue(classifier.isCoupon("Value: $50"))
    }

    @Test
    fun isCoupon_returnsTrue_forEuroSymbol() {
        assertTrue(classifier.isCoupon("Wert: €75"))
    }

    @Test
    fun isCoupon_returnsTrue_forNISText() {
        assertTrue(classifier.isCoupon("Balance: 200 NIS"))
    }

    @Test
    fun isCoupon_returnsTrue_forNISTextLowercase() {
        assertTrue(classifier.isCoupon("Balance: 200 nis"))
    }

    @Test
    fun isCoupon_returnsTrue_forHebrewShekel() {
        assertTrue(classifier.isCoupon("100 ש\"ח"))
    }

    // ========== Code Pattern Tests ==========

    @Test
    fun isCoupon_returnsTrue_forNumericCodePattern() {
        assertTrue(classifier.isCoupon("Your code is 1234-5678"))
    }

    @Test
    fun isCoupon_returnsTrue_forAlphanumericCodePattern() {
        assertTrue(classifier.isCoupon("Redeem with ABCD-1234"))
    }

    @Test
    fun isCoupon_returnsTrue_forLongCodePattern() {
        assertTrue(classifier.isCoupon("Code: 12345678-90123456"))
    }

    // ========== Negative Tests ==========

    @Test
    fun isCoupon_returnsFalse_forPlainText() {
        assertFalse(classifier.isCoupon("This is just a regular message about shopping"))
    }

    @Test
    fun isCoupon_returnsFalse_forEmptyString() {
        assertFalse(classifier.isCoupon(""))
    }

    @Test
    fun isCoupon_returnsFalse_forRandomNumbers() {
        assertFalse(classifier.isCoupon("Meeting at 3pm, call 555-1234"))
    }

    @Test
    fun isCoupon_returnsFalse_forDateLikePattern() {
        assertFalse(classifier.isCoupon("Date: 2024-01-15"))
    }

    // ========== Case Insensitivity Tests ==========

    @Test
    fun isCoupon_isCaseInsensitive_forKeywords() {
        assertTrue(classifier.isCoupon("COUPON"))
        assertTrue(classifier.isCoupon("Coupon"))
        assertTrue(classifier.isCoupon("coupon"))
    }
}
