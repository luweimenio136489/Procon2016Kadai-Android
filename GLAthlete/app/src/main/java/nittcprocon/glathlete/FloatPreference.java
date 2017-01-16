package nittcprocon.glathlete;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

public class FloatPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, TextWatcher {

    private static final String TAG = "FloatPreference";
    private Context context;
    private SharedPreferences sharedPreferences;
    private EditText editText;
    private SeekBar seekBar;
    private boolean editTextFromUser = true;
    private float value, min, max;
    private static final int SEEKBAR_MAX = 1000;
    private static final String XMLNS_CUSTOM = "http://schemas.android.com/apk/res/nittcprocon.glathlete";

    public FloatPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FloatPreference(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);

        Log.d(TAG, "FloatPreference()");
        Log.d(TAG, "key: " + this.getKey());

        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        value = sharedPreferences.getFloat(this.getKey(), 0.0f); // TODO: use default value
        Log.d(TAG, "value from SharedPreferences: " + value);

        min = attributeSet.getAttributeFloatValue(XMLNS_CUSTOM, "minValue", 0.0f);
        max = attributeSet.getAttributeFloatValue(XMLNS_CUSTOM, "maxValue", 1.0f);
        Log.d(TAG, "min: " + min + ", max: " + max);

        setDialogLayoutResource(R.layout.preference_float);

        // なぜか勝手にnullになるので再設定
        setPositiveButtonText("OK");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        editText = (EditText)v.findViewById(R.id.editText);
        seekBar = (SeekBar)v.findViewById(R.id.seekBar);

        editText.addTextChangedListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(SEEKBAR_MAX);

        updateEditText();
        updateSeekBar();
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        updateEditText();
        updateSeekBar();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.d(TAG, "saving " + this.getKey() + " = " + value);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(this.getKey(), value);
            editor.apply();
        } else {
            Log.d(TAG, "save cancelled");
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Log.d(TAG, "onGetDefaultValue, index: " + index);
        float defaultValue = a.getFloat(index, 0.0f);
        Log.d(TAG, "default value: " + defaultValue);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
        } else {
            if (defaultValue != null) {
                value = (float)defaultValue;
                Log.d(TAG, "Set default value " + value);
            }
        }
    }

    // OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            value = progressToValue(progress);
            Log.d(TAG, "Setting temporary value to " + value + " from SeekBar");
            if (editText != null) {
                updateEditText();
            }
        }
    }

    // TextWatcher
    public void afterTextChanged(Editable s) {
        if (editTextFromUser) {
            String str = s.toString();
            if (!str.equals("")) {
                value = Float.parseFloat(s.toString());
                Log.d(TAG, "Setting temporary value to " + value + " from EditText");
                if (seekBar != null) {
                    updateSeekBar();
                }
            }
        }
    }

    private int valueToProgress(float v) {
        float valuew = max - min;
        return (int)(((v - min) / valuew) * SEEKBAR_MAX);
    }

    private float progressToValue(int p) {
        float valuew = max - min;
        return min + valuew * p / SEEKBAR_MAX;
    }

    private void updateEditText() {
        Log.d(TAG, "updating EditText, value " + value);
        editTextFromUser = false;
        editText.setText(Float.toString(value));
        editTextFromUser = true;
    }

    private void updateSeekBar() {
        Log.d(TAG, "updating SeekBar, value " + value);
        seekBar.setProgress(valueToProgress(value));
    }

    public void onStartTrackingTouch(SeekBar seekBar) {}

    public void onStopTrackingTouch(SeekBar seekBar) {}

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    public void onTextChanged(CharSequence s, int start, int before, int count) {}

}
