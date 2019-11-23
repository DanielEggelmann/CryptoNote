package de.danieleggelmann.cryptonote;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import de.danieleggelmann.cryptonote.library.NotebookDirectory;
import de.danieleggelmann.cryptonote.library.NotebookElement;
import de.danieleggelmann.cryptonote.library.OperationFailedException;

public class NotebookRecyclerAdapter extends RecyclerView.Adapter<NotebookRecyclerAdapter.NotebookRecyclerViewHolder> {

    private NotebookDirectory mDirectory;

    public static class NotebookRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public NotebookRecyclerViewHolder(TextView view) {
            super(view);

            textView = view;
        }
    }

    public NotebookRecyclerAdapter(NotebookDirectory directory)
    {
        mDirectory = directory;
    }

    @Override
    public NotebookRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.notebook_viewholder, parent, false);

        NotebookRecyclerViewHolder viewHolder = new NotebookRecyclerViewHolder(textView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NotebookRecyclerViewHolder holder, int position) {
        NotebookElement element = null;
        try {
            element = mDirectory.GetElement(position);

            holder.textView.setText(element.GetName());

        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        try {
            return mDirectory.GetElementCount();
        } catch (OperationFailedException e) {
            e.printStackTrace();

            return 0;
        }
    }
}
