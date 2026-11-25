package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProjectActivity extends AppCompatActivity implements ProjectHost {

    private int projectId;
    private Project currentProject;

    private TextView txtProjectTag, txtProjectName;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private FilterRequest pendingFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // the id comes from HomeActivity through the intent. the one working project is looked up here
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        currentProject = AppData.findProjectById(projectId);
        if (currentProject == null) {
            finish();
            return;
        }

        txtProjectTag = findViewById(R.id.txtHeaderTag);
        txtProjectName = findViewById(R.id.txtHeaderName);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // back arrow just goes back to the existing HomeActivity. avatars and Invite are only ui
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        viewPager.setAdapter(new ProjectPagerAdapter(this, projectId));
        // keep all 4 fragments alive so search/filter/sort are kept while switching tabs
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitle(position));
        }).attach();

        showHeader();
        sendResultToHome();
    }

    private String tabTitle(int position) {
        if (position == TAB_OVERVIEW) return "Overview";
        if (position == TAB_TASKS) return "Tasks (" + currentProject.getTotalTasks() + ")";
        if (position == TAB_FILES) return "Files (" + currentProject.getAttachments().size() + ")";
        return "Discussion (" + currentProject.getCommentCount() + ")";
    }

    private void showHeader() {
        txtProjectTag.setText(currentProject.getTag());
        txtProjectName.setText(currentProject.getName());
    }

    private void updateTabTitles() {
        for (int i = 0; i < 4; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(tabTitle(i));
            }
        }
    }

    // the result is set early, so the arrow and the system Back button both return it
    private void sendResultToHome() {
        Intent result = new Intent();
        result.putExtra("PROJECT_ID", projectId);
        setResult(RESULT_OK, result);
    }

    // ---- ProjectHost: called by the fragments ----

    @Override
    public void onProjectChanged() {
        showHeader();
        updateTabTitles();
        sendResultToHome();
    }

    @Override
    public void openTab(int index) {
        viewPager.setCurrentItem(index);
    }

    @Override
    public void openTasksWithFilter(Task.Status status, boolean overdue) {
        FilterRequest request = new FilterRequest();
        request.status = status;
        request.overdue = overdue;
        pendingFilter = request;
        // the tab indicator follows because TabLayoutMediator listens to the ViewPager2
        viewPager.setCurrentItem(TAB_TASKS);
    }

    @Override
    public FilterRequest takePendingFilter() {
        FilterRequest request = pendingFilter;
        pendingFilter = null;
        return request;
    }
}
