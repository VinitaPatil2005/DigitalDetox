package com.example.digitaldetox.ui

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CouponUtils {
    private const val PREF_NAME = "CouponPrefs"
    private const val KEY_COUPONS = "SavedCoupons"

    fun saveCoupon(context: Context, newCoupon: Coupon) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val couponList = loadCoupons(context).toMutableList()
        couponList.add(newCoupon)
        val json = Gson().toJson(couponList)
        prefs.edit().putString(KEY_COUPONS, json).apply()
    }

    fun loadCoupons(context: Context): List<Coupon> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_COUPONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Coupon>>() {}.type
        return Gson().fromJson(json, type)
    }
}
