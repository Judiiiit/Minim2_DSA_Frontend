package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.GroupAdapter;
import com.example.android_proyecto.Models.Group;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupActivity extends AppCompatActivity {

    private RecyclerView recyclerGroups;
    private SessionManager session;

    private final Set<Integer> joinedGroupIds = new HashSet<>(); // se rellena desde prefs
    private GroupAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        session = new SessionManager(this);

        recyclerGroups = findViewById(R.id.recyclerGroups);
        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        joinedGroupIds.clear();
        joinedGroupIds.addAll(session.getJoinedGroups());

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void loadGroups() {
        ApiService api = RetrofitClient.getApiService();

        api.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    joinedGroupIds.clear();
                    joinedGroupIds.addAll(session.getJoinedGroups());

                    adapter = new GroupAdapter(
                            response.body(),
                            joinedGroupIds,
                            new GroupAdapter.OnJoinClickListener() {
                                @Override
                                public void onJoin(Group group) {
                                    if (joinedGroupIds.contains(group.getId())) {
                                        Toast.makeText(GroupActivity.this,
                                                "Ya estás en ese grupo",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        joinGroup(group.getId());
                                    }
                                }

                                @Override
                                public void onAlreadyJoined(Group group) {
                                    Toast.makeText(GroupActivity.this,
                                            "Ya estás en ese grupo",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                    recyclerGroups.setAdapter(adapter);

                } else {
                    Toast.makeText(GroupActivity.this,
                            "Could not load groups", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(GroupActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup(int groupId) {
        ApiService api = RetrofitClient.getApiService();

        String token = session.getToken(); // sin Bearer
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        api.joinGroup(token, groupId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.code() == 200) {
                    session.addJoinedGroup(groupId);

                    joinedGroupIds.add(groupId);
                    if (adapter != null) adapter.notifyDataSetChanged();

                    Toast.makeText(GroupActivity.this,
                            "Joined group!", Toast.LENGTH_SHORT).show();

                } else if (response.code() == 409) {
                    session.addJoinedGroup(groupId);
                    joinedGroupIds.add(groupId);
                    if (adapter != null) adapter.notifyDataSetChanged();

                    Toast.makeText(GroupActivity.this,
                            "Ya estás en ese grupo",
                            Toast.LENGTH_SHORT).show();

                } else if (response.code() == 401) {
                    Toast.makeText(GroupActivity.this, "Unauthorized", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(GroupActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GroupActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
