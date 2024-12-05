package com.app.quickcall.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.quickcall.databinding.FragmentProfileBinding;
import com.app.quickcall.repository.MainRepository;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    MainRepository mainRepository;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainRepository = MainRepository.getInstance();

        // get information from database
        binding.btnLogout.setOnClickListener( v -> {
            mainRepository.logout();
            getActivity().finish();
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}