package uk.ac.cam.cl.waytotheclinic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TextInstructionsAdapter extends ArrayAdapter<Instruction>{

    private int layoutResource;

    public TextInstructionsAdapter(@NonNull Context context, int resource, @NonNull List<Instruction> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(layoutResource, null);
        }

        Instruction instruction = getItem(position);

        if (instruction != null) {
            ImageView instruction_icon = (ImageView) view.findViewById(R.id.instruction_icon);
            TextView instruction_text = (TextView) view.findViewById(R.id.instruction_text);

            if (instruction_icon != null) {
                instruction_icon.setImageResource(instruction.getInstructionIcon());
            }
            if (instruction_text != null) {
                instruction_text.setText(instruction.getInstructionText());
            }
        }

        return view;

    }
}
