package com.example.digitaldetox.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.digitaldetox.R

class SliderAdapter(private val context: Context) : PagerAdapter() {

    private val descriptions = arrayOf(
        "Track your screen time and understand your habits.",
        "Set goals to reduce mobile usage and boost focus.",
        "Take mindful breaks and build a healthier lifestyle."
    )

    private val background = R.drawable.splashbg // single background for all slides

    override fun getCount(): Int = descriptions.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.slide_layout, container, false)

        val backgroundImage = view.findViewById<ImageView>(R.id.slideBackground)
        val textView = view.findViewById<TextView>(R.id.slideText)

        // If you plan to use different backgrounds in future
        backgroundImage.setImageResource(R.drawable.splashbg)

        // Set the slide text
        textView.text = descriptions[position]

        container.addView(view)
        return view
    }




    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
