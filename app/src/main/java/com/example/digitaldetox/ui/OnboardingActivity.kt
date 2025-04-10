package com.example.digitaldetox.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.digitaldetox.MainActivity
import com.example.digitaldetox.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var startBtn: Button
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var dots: Array<TextView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dotsLayout)
        startBtn = findViewById(R.id.btnStart)

        sliderAdapter = SliderAdapter(this)
        viewPager.adapter = sliderAdapter

        addDots(0)
        viewPager.addOnPageChangeListener(changeListener)

        startBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun addDots(position: Int) {
        dots = arrayOfNulls(3)
        dotsLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[position]?.setTextColor(resources.getColor(android.R.color.white, null))
        }
    }

    private val changeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            addDots(position)

            if (position == 2) {
                startBtn.visibility = Button.VISIBLE
            } else {
                startBtn.visibility = Button.GONE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }
}
