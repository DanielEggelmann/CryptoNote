package de.danieleggelmann.cryptonote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class NewNotebookElementDialogFragment extends DialogFragment {

    private NewNotebookElementDialogListener mListener;
    private EditText edit_name;

    public interface NewNotebookElementDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String name);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_notebookelement, null);
        edit_name = view.findViewById(R.id.dialog_new_notebookelement_edit_name);

        builder.setView(view)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDialogPositiveClick(NewNotebookElementDialogFragment.this, edit_name.getText().toString());
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
