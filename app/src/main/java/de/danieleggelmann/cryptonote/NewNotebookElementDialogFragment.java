package de.danieleggelmann.cryptonote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class NewNotebookElementDialogFragment extends DialogFragment {

    private NewNotebookElementDialogListener mListener;

    private EditText edit_name;
    private Spinner spinner_type;

    public interface NewNotebookElementDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String name, int type);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_notebookelement, null);
        edit_name = view.findViewById(R.id.dialog_new_notebookelement_edit_name);
        spinner_type = view.findViewById(R.id.dialog_new_notebookelement_spinner_type);

        final ArrayAdapter<CharSequence> spinner_typeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.notebookelement_types, android.R.layout.simple_spinner_item);
        spinner_typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(spinner_typeAdapter);

        builder.setView(view)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDialogPositiveClick(NewNotebookElementDialogFragment.this, edit_name.getText().toString(), spinner_type.getSelectedItemPosition());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NewNotebookElementDialogFragment.this.getDialog().cancel();
                        mListener.onDialogNegativeClick(NewNotebookElementDialogFragment.this);
                    }
                });



        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mListener = (NewNotebookElementDialogListener) context;
    }
}
