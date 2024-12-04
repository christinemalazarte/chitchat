package com.app.quickcall.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.quickcall.R;
import com.app.quickcall.adapter.ContactItemAdapter;
import com.app.quickcall.databinding.FragmentFirstBinding;
import com.app.quickcall.utils.CallListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ContactItemAdapter adapter;
    private List<String> itemList;
    private Context context;
    CallListener listener;

    public FirstFragment(Context context, CallListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemList = new ArrayList<>();
        adapter = new ContactItemAdapter(context, itemList, listener);

        // Set up RecyclerView
        binding.contactListView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.contactListView.setAdapter(adapter);
        itemList.clear(); // Clear the list to avoid duplicates


        // Add items to the ArrayList
//        itemList.add("Apple");
//        itemList.add("Banana");
//        itemList.add("Cherry");
//        itemList.add("Date");
//
//        adapter.notifyDataSetChanged();


        // Fetch data from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String key = childSnapshot.getKey(); // Get the key
                    itemList.add(key); // Add to list
                }
                adapter.notifyDataSetChanged(); // Notify adapter after updating list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear the list
//        if (itemList != null) {
//            itemList.clear();
//            adapter.notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
//        }
    }

}
