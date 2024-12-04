package com.app.quickcall.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.quickcall.R;
import com.app.quickcall.view.CallActivity;

import java.util.List;

public class ContactItemAdapter extends RecyclerView.Adapter<ContactItemAdapter.ItemViewHolder> {

    private final List<String> contacts;
    private Context context;

    public ContactItemAdapter(Context context, List<String> contacts) {
        this.contacts = contacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
//        Contact contact = contacts.get(position);

        // Set contact name
        holder.textViewContactName.setText(contacts.get(position));

        // Set placeholder image for the profile picture
        holder.imageViewContactPhoto.setImageResource(R.drawable.ic_profile_placeholder);

        // Handle call icon click
        holder.imageViewCall.setOnClickListener(v -> {
            // Implement call functionality (e.g., open dialer)
            System.out.println("Call clicked for: " + contacts.get(position));

            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("contact_name", contacts.get(position)); // Pass contact name to the new activity
            context.startActivity(intent);
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
