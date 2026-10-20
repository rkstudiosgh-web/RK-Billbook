package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CassetteItem
import com.example.data.model.PaymentInstallment
import com.example.data.model.Rasam
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    // List<String>
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) { }
        return list
    }

    // List<Rasam>
    @TypeConverter
    fun fromRasamList(list: List<Rasam>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { rasam ->
            val obj = JSONObject()
            obj.put("id", rasam.id)
            obj.put("name", rasam.name)
            obj.put("date", rasam.date)
            obj.put("time", rasam.time)
            obj.put("venue", rasam.venue)
            val camArray = JSONArray()
            rasam.cameras.forEach { camArray.put(it) }
            obj.put("cameras", camArray)
            obj.put("routeNote", rasam.routeNote)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toRasamList(data: String?): List<Rasam> {
        if (data.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<Rasam>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val cams = mutableListOf<String>()
                if (obj.has("cameras")) {
                    val camArr = obj.getJSONArray("cameras")
                    for (c in 0 until camArr.length()) {
                        cams.add(camArr.getString(c))
                    }
                }
                list.add(
                    Rasam(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        date = obj.optString("date"),
                        time = obj.optString("time"),
                        venue = obj.optString("venue"),
                        cameras = cams,
                        routeNote = obj.optString("routeNote")
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    // List<PaymentInstallment>
    @TypeConverter
    fun fromPaymentList(list: List<PaymentInstallment>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("date", p.date)
            obj.put("amount", p.amount)
            obj.put("note", p.note)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toPaymentList(data: String?): List<PaymentInstallment> {
        if (data.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<PaymentInstallment>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PaymentInstallment(
                        id = obj.optString("id"),
                        date = obj.optString("date"),
                        amount = obj.optDouble("amount", 0.0),
                        note = obj.optString("note", "Jama Next")
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    // List<CassetteItem>
    @TypeConverter
    fun fromCassetteList(list: List<CassetteItem>?): String {
        if (list == null) return "[]"
        val array = JSONArray()
        list.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("label", c.label)
            obj.put("type", c.type)
            obj.put("quantity", c.quantity)
            obj.put("rate", c.rate)
            obj.put("amount", c.amount)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toCassetteList(data: String?): List<CassetteItem> {
        if (data.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<CassetteItem>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CassetteItem(
                        id = obj.optString("id"),
                        label = obj.optString("label"),
                        type = obj.optString("type", "VHS Big Tape"),
                        quantity = obj.optInt("quantity", 1),
                        rate = obj.optDouble("rate", 350.0),
                        amount = obj.optDouble("amount", 350.0)
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }
}
