package com.mervynm.nom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mervynm.nom.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    BottomNavigationView bottomNavigationBar;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupVariables();
        setupToolbar();
        setUpBottomNavigationBar();
    }

    private void setupVariables() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationBar = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        toolbar.setTitle("Nom");
    }

    private void setUpBottomNavigationBar() {
        bottomNavigationBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new HomeFragment();
                if (item.getItemId() == R.id.action_home) {
                    fragment = new HomeFragment();
                } else if (item.getItemId() == R.id.action_compose) {
                    fragment = new HomeFragment();
                } else if (item.getItemId() == R.id.action_profile) {
                    fragment = new HomeFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationBar.setSelectedItemId(R.id.action_home);
    }
}