package uk.ac.cam.cl.waytotheclinic;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.util.AttributeSet;

import java.util.HashMap;

public class CustomAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public CustomAutoCompleteTextView(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode)
    {
        String newText = text.toString();
        if (newText.contains(" "))
        {
            int lastIndex = newText.lastIndexOf(" ");
            if (lastIndex != newText.length() - 1)
            {
                newText = newText.substring(lastIndex + 1).trim();
                if (newText.length() >= getThreshold())
                {
                    text = newText;
                }
            }
        }
        super.performFiltering(text, keyCode);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            showDropDown();
            performFiltering(getText(), 0);
        }
    }

    /** Returns the country name corresponding to the selected item */
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        /** Each item in the autocompetetextview suggestion list is a hashmap object */
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("txt");
    }
}
