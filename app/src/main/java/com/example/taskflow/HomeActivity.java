package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_PROJECT = 1;

    private Button btnNewProject;
    private EditText edtSearch;
    private TextView txtShowing, txtNoProjects;
    private RecyclerView recyclerProjects;

    private ProjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AppData.init();

        btnNewProject = findViewById(R.id.btnNewProject);
        edtSearch = findViewById(R.id.edtSearchProjects);
        txtShowing = findViewById(R.id.txtShowingProjects);
        txtNoProjects = findViewById(R.id.txtNoProjects);
        recyclerProjects = findViewById(R.id.recyclerProjects);

        // clicking a row opens the project by its id
        adapter = new ProjectAdapter(projectId -> openProject(projectId));
        recyclerProjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerProjects.setAdapter(adapter);

        btnNewProject.setOnClickListener(v -> showNewProjectDialog());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                refreshProjectList();
            }
        });

        refreshProjectList();
    }

    private void showNewProjectDialog() {
        DialogHelper.showProjectDialog(this, null, (name, tag, description, attachment) -> {
            Project project = new Project(tag, name, description, 0);
            if (attachment != null) {
                project.linkCatalogItem(attachment);
            }
            AppData.addProject(project);
            refreshProjectList();
        });
    }

    // builds a brand new list of row copies and hands it to the adapter.
    // DiffUtil inside ListAdapter finds which rows really changed, so notifyDataSetChanged is never used
    private void refreshProjectList() {
        String query = edtSearch.getText().toString().trim().toLowerCase();

        ArrayList<ProjectRow> rows = new ArrayList<>();
        for (Project project : AppData.getProjectList()) {
            boolean nameMatch = project.getName().toLowerCase().contains(query);
            boolean tagMatch = project.getTag().toLowerCase().contains(query);
            if (query.isEmpty() || nameMatch || tagMatch) {
                // only the first card shows the invite pill
                rows.add(new ProjectRow(project, rows.size() == 0));
            }
        }
        adapter.submitList(rows);

        txtShowing.setText("Showing " + rows.size() + " projects");
        if (rows.size() == 0) {
            txtNoProjects.setVisibility(View.VISIBLE);
        } else {
            txtNoProjects.setVisibility(View.GONE);
        }
    }

    private void openProject(int projectId) {
        Intent intent = new Intent(HomeActivity.this, ProjectActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        startActivityForResult(intent, REQUEST_PROJECT);
    }

    // ProjectActivity sends back the id of the project it changed. we find that project by id
    // and submit a fresh list, so only the changed row is redrawn
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PROJECT && resultCode == RESULT_OK && data != null) {
            int projectId = data.getIntExtra("PROJECT_ID", -1);
            Project updated = AppData.findProjectById(projectId);
            if (updated != null) {
                refreshProjectList();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProjectList();
    }
}
