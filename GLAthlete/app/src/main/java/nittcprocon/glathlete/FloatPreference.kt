@file:JvmName("FloatPreference")

package nittcprocon.glathlete

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Bundle
import android.preference.DialogPreference
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.SeekBar

class FloatPreference @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int = 0) :
        DialogPreference(context, attributeSet, defStyleAttr), SeekBar.OnSeekBarChangeListener, TextWatcher {
    private val TAG = "FloatPreference"
    private val SEEKBAR_MAX = 1000
    private val XMLNS_CUSTOM = "http://schemas.android.com/apk/res/nittcprocon.glathlete"
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyContexts.applicationContext)
    private var editText: EditText? = null
    private var seekBar: SeekBar? = null
    private var editTextFromUser = true
    private var value = preferences.getFloat(this.key, 0.0f) // TODO: use default value
    private val min = attributeSet.getAttributeFloatValue(XMLNS_CUSTOM, "minValue", 0.0f)
    private val max = attributeSet.getAttributeFloatValue(XMLNS_CUSTOM, "maxValue", 1.0f)

    init {
        Log.d(TAG, "FloatPreference()")
        Log.d(TAG, "key: " + this.key)
        Log.d(TAG, "value from SharedPreferences: " + value)
        Log.d(TAG, "min: $min, max: $max")

        dialogLayoutResource = R.layout.preference_float

        // なぜか勝手にnullになるので再設定
        positiveButtonText = "OK"
        negativeButtonText = "Cancel"
    }

    override fun onBindDialogView(v: View) {
        super.onBindDialogView(v)

        editText = v.findViewById(R.id.editText) as EditText
        editText!!.addTextChangedListener(this)

        seekBar = v.findViewById(R.id.seekBar) as SeekBar
        seekBar!!.setOnSeekBarChangeListener(this)
        seekBar!!.max = SEEKBAR_MAX

        value = preferences.getFloat(this.key, 0.0f) // TODO: use default value

        updateEditText()
        updateSeekBar()
    }

    override fun showDialog(state: Bundle) {
        super.showDialog(state)

        updateEditText()
        updateSeekBar()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            Log.d(TAG, "saving " + this.key + " = " + value)
            val editor = preferences.edit()
            editor.putFloat(this.key, value)
            editor.apply()
        } else {
            Log.d(TAG, "save cancelled")
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        Log.d(TAG, "onGetDefaultValue, index: " + index)
        val defaultValue = a.getFloat(index, 0.0f)
        Log.d(TAG, "default value: " + defaultValue)
        return defaultValue
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (restoreValue) {
            value = preferences.getFloat(key, 0.0f)
            Log.d(TAG, "restore value " + value)
        } else {
            if (defaultValue != null) {
                value = defaultValue as Float
                Log.d(TAG, "Set default value " + value)
            }
        }
    }

    // OnSeekBarChangeListener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            value = progressToValue(progress)
            Log.d(TAG, "Setting temporary value to $value from SeekBar")
            if (editText != null) {
                updateEditText()
            }
        }
    }

    // TextWatcher
    override fun afterTextChanged(s: Editable) {
        if (editTextFromUser) {
            val str = s.toString()
            if (str != "") {
                value = java.lang.Float.parseFloat(s.toString())
                Log.d(TAG, "Setting temporary value to $value from EditText")
                if (seekBar != null) {
                    updateSeekBar()
                }
            }
        }
    }

    private fun valueToProgress(v: Float): Int {
        val valuew = max - min
        return ((v - min) / valuew * SEEKBAR_MAX).toInt()
    }

    private fun progressToValue(p: Int): Float {
        val valuew = max - min
        return min + valuew * p / SEEKBAR_MAX
    }

    private fun updateEditText() {
        Log.d(TAG, "updating EditText, value " + value)
        editTextFromUser = false
        editText!!.setText(java.lang.Float.toString(value))
        editTextFromUser = true
    }

    private fun updateSeekBar() {
        Log.d(TAG, "updating SeekBar, value " + value)
        seekBar!!.progress = valueToProgress(value)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
}
