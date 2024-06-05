package com.pattern.acquaintances.ui.home;

import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.pattern.acquaintances.MainActivity;
import com.pattern.acquaintances.databinding.FragmentHomeBinding;
import com.pattern.acquaintances.model.Account;
import com.pattern.acquaintances.model.AuthManager;
import com.pattern.acquaintances.model.DBManager;
import com.pattern.acquaintances.model.StorageManager;

import java.util.function.Function;
import java.io.IOException;
import java.io.InputStream;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private String email;
    private String pass;
    Handler mainHandler;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        StorageManager storage = new StorageManager();
        mainHandler = new Handler();
        storage.setGetProfileOnCompete(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    new ImageDownloader(task.getResult().toString()).start();
                }
            }
        });
        DBManager db = new DBManager(new Function<Account, Void>() {
            @Override
            public Void apply(Account account) {
                String name = account.getFirstName() + " " + account.getLastName();
                Log.i("MainActivity", name);
                binding.textHome.setText(name);
                storage.getProfilePhoto();
                return null;
            }
        });
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        email = ((MainActivity)getActivity()).getEmail();
        pass = ((MainActivity)getActivity()).getPass();
        AuthManager auth = new AuthManager();
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

    class ImageDownloader extends Thread{
        String URL;
        Bitmap bitmap;
        ImageDownloader(String url){
            URL = url;
        }

        @Override
        public void run(){
            super.run();
            InputStream stream;
            try {
                stream = new java.net.URL(URL).openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bitmap = BitmapFactory.decodeStream(stream);

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    binding.imageView2.setImageBitmap(bitmap);
                    Log.i("Storage", "Image set");
                }
            });
        }
    }
}