package com.example.taskflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.taskflow.fragments.DiscussionFragment;
import com.example.taskflow.fragments.FilesFragment;
import com.example.taskflow.fragments.OverviewFragment;
import com.example.taskflow.fragments.TasksFragment;

// gives ViewPager2 the right fragment for each tab
public class ProjectPagerAdapter extends FragmentStateAdapter {

    private int projectId;

    public ProjectPagerAdapter(FragmentActivity activity, int projectId) {
        super(activity);
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return OverviewFragment.newInstance(projectId);
        if (position == 1) return TasksFragment.newInstance(projectId);
        if (position == 2) return FilesFragment.newInstance(projectId);
        return DiscussionFragment.newInstance(projectId);
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
