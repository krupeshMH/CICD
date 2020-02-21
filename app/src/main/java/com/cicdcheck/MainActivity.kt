package com.cicdcheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCenter.start(
            application,
            "8d4adc0c-0bba-4ea5-bb2c-aa4656c1e2a7",
            Analytics::class.java,
            Crashes::class.java
        );
        val feature = Crashes.hasCrashedInLastSession()
        feature.thenAccept {
            if (it) {
                Toast.makeText(this, "It has crashed previously", Toast.LENGTH_LONG).show()
            }
        }
        calculateButton.setOnClickListener {
            Crashes.generateTestCrash()
            try {
                val interestRate = interestEditText.text.toString().toFloat()
                val currentAge = ageEditText.text.toString().toInt()
                val retirementAge = retirementEditText.text.toString().toInt()
                val monthly = monthlySavingsEditText.text.toString().toFloat()
                val current = currentEditText.text.toString().toFloat()

                val properties: HashMap<String, String> = HashMap<String, String>()
                properties.put("interest_rate", interestRate.toString())
                properties.put("current_age", currentAge.toString())
                properties.put("retirement_age", retirementAge.toString())
                properties.put("monthly_savings", monthly.toString())
                properties.put("current_savings", current.toString())

                if (interestRate <= 0) {
                    Analytics.trackEvent("wrong_interest_rate", properties)
                }
                if (retirementAge <= currentAge) {
                    Analytics.trackEvent("wrong_age", properties)
                }

                val futureSavings = calculateRetirement(
                    interestRate,
                    current,
                    monthly,
                    (retirementAge - currentAge) * 12
                )

                resultTextView.text =
                    "At the current rate of $interestRate%, saving \$$monthly a month you will have \$${String.format(
                        "%f",
                        futureSavings
                    )} by $retirementAge."
            } catch (ex: Exception) {
                Analytics.trackEvent(ex.message)
            }
        }
    }

    fun calculateRetirement(
        interestRate: Float,
        currentSavings: Float,
        monthly: Float,
        numMonths: Int
    ): Float {
        var futureSavings = currentSavings * (1 + (interestRate / 100 / 12)).pow(numMonths)

        for (i in 1..numMonths) {
            futureSavings += monthly * (1 + (interestRate / 100 / 12)).pow(i)
        }

        return futureSavings
    }
}

