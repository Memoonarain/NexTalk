package com.gamevision.nextalk.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamevision.nextalk.R;
import com.gamevision.nextalk.adapters.UserAdapter;
import com.gamevision.nextalk.auth.LoginActivity;
import com.gamevision.nextalk.auth.ProfileActivity;
import com.gamevision.nextalk.auth.ProfileUpdateActivity;
import com.gamevision.nextalk.models.UserModel;
import com.gamevision.nextalk.models.UserWithLastMessage;
import com.gamevision.nextalk.utills.PrivacyPolicyActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText searchView;
    private MainViewModel viewModel;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    MaterialToolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchView = findViewById(R.id.etSearch);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        recyclerView = findViewById(R.id.rvChats);
        adapter = new UserAdapter(this, user -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("otherUserId", user.getUid());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        viewModel.getUsersWithMessages().observe(this, adapter::updateData);
        viewModel.loadUsers();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchUsers(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        viewModel.getSearchResults().observe(this, users -> {
            List<UserWithLastMessage> emptyUser = new ArrayList<>();
            emptyUser.add(new UserWithLastMessage(new UserModel(), null));
            adapter.updateData(emptyUser);
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Settings) {
            // Correct usage
            Intent intent = new Intent(MainActivity.this, ProfileUpdateActivity.class);
            startActivity(intent);
            return true;
        } else if (id==R.id.Profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.Logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }else if (id == R.id.PrivacyPolicy) {
            startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
            return true;
        }else
        return super.onOptionsItemSelected(item);

        return false;
    }

}