package com.app.quickcall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.quickcall.R;
import com.app.quickcall.utils.CallListener;

import java.util.List;

public class ContactItemAdapter extends RecyclerView.Adapter<ContactItemAdapter.ItemViewHolder> {

    private final List<String> contacts;
    private Context context;
    private CallListener listener;

    public ContactItemAdapter(Context context, List<String> contacts, CallListener listener) {
        this.contacts = contacts;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Set contact name
        holder.textViewContactName.setText(contacts.get(position));

        // Set placeholder image for the profile picture
        holder.imageViewContactPhoto.setImageResource(R.drawable.ic_user);

        // Handle call icon click
        holder.imageViewCall.setOnClickListener(v -> {
            // Implement call functionality (e.g., open dialer)
            String name = contacts.get(position);
            listener.startCall(name);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContactName;
        ImageView imageViewContactPhoto;
        ImageView imageViewCall;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContactName = itemView.findViewById(R.id.textViewContactName);
            imageViewContactPhoto = itemView.findViewById(R.id.imageViewContactPhoto);
            imageViewCall = itemView.findViewById(R.id.imageViewCall);
        }
    }
}
