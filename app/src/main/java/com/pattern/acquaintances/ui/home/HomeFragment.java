package com.pattern.acquaintances.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.pattern.acquaintances.MainActivity;
import com.pattern.acquaintances.databinding.FragmentHomeBinding;
import com.pattern.acquaintances.model.Account;
import com.pattern.acquaintances.model.AuthManager;
import com.pattern.acquaintances.model.DBManager;
import com.pattern.acquaintances.model.StorageManager;

import java.util.function.Function;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private String email;
    private String pass;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        /*storage.setGetProfileOnCompete(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    binding.imageView2.setImageURI(task.getResult());
                }
            }
        });*/

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        email = ((MainActivity)getActivity()).getEmail();
        pass = ((MainActivity)getActivity()).getPass();
        DBManager db = new DBManager(new Function<Account, Void>() {
            @Override
            public Void apply(Account account) {
                String name = account.getFirstName() + " " + account.getLastName();
                Log.i("MainActivity", name);
                binding.textView2.setText(name);
                return null;
            }
        });
        AuthManager auth = new AuthManager();
        StorageManager storage = new StorageManager();
        auth.signIn(email, pass);
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}