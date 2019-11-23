package de.danieleggelmann.cryptonote;

import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotebookRecyclerAdapter extends RecyclerView.Adapter<NotebookRecyclerAdapter.NotebookRecyclerViewHolder> {

    private Notebook mNotebook;

    public static class NotebookRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public NotebookRecyclerViewHolder(TextView view) {
            super(view);

            textView = view;
        }
    }

    public NotebookRecyclerAdapter(Notebook notebook)
    {
        mNotebook = notebook;
    }

    @Override
    public NotebookRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.notebook_viewholder, parent, false);

        NotebookRecyclerViewHolder viewHolder = new NotebookRecyclerViewHolder(textView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NotebookRecyclerViewHolder holder, int position) {
        DatabaseElement element = mNotebook.GetContent()[position];
        if(element.IsNotebook())
        {
            holder.textView.setText(element.ToNotebook().GetName());
        }
        else
        {
            holder.textView.setText(element.ToNote().GetTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mNotebook.GetContent().length;
    }
}
