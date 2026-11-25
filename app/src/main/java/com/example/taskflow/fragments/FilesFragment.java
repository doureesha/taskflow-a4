package com.example.taskflow.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.AppData;
import com.example.taskflow.AttachmentAdapter;
import com.example.taskflow.AttachmentLink;
import com.example.taskflow.AttachmentRow;
import com.example.taskflow.CatalogItem;
import com.example.taskflow.DialogHelper;
import com.example.taskflow.Project;
import com.example.taskflow.ProjectHost;
import com.example.taskflow.R;

import java.util.ArrayList;

public class FilesFragment extends Fragment {

    private static final int TYPE_ALL = 0;
    private static final int TYPE_IMAGES = 1;
    private static final int TYPE_TEXT = 2;

    private int projectId;
    private ProjectHost host;

    private String query = "";
    private int typeFilter = TYPE_ALL;
    private boolean syncing = false;

    private EditText edtSearch;
    private TextView chipAll, chipImages, chipText, txtShowing;
    private View layoutNoFiles, layoutNoMatch;
    private Button btnAddAttachment, btnAddAttachmentEmpty, btnResetEmpty;
    private RecyclerView recyclerFiles;

    private AttachmentAdapter adapter;

    public static FilesFragment newInstance(int projectId) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putInt("PROJECT_ID", projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectId = getArguments().getInt("PROJECT_ID");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearchFiles);
        chipAll = view.findViewById(R.id.chipAll);
        chipImages = view.findViewById(R.id.chipImages);
        chipText = view.findViewById(R.id.chipText);
        txtShowing = view.findViewById(R.id.txtShowingFiles);
        layoutNoFiles = view.findViewById(R.id.layoutNoFiles);
        layoutNoMatch = view.findViewById(R.id.layoutNoFilesMatch);
        btnAddAttachment = view.findViewById(R.id.btnAddAttachment);
        btnAddAttachmentEmpty = view.findViewById(R.id.btnAddAttachmentEmpty);
        btnResetEmpty = view.findViewById(R.id.btnResetFiles);
        recyclerFiles = view.findViewById(R.id.recyclerFiles);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (syncing) return;
                query = s.toString();
                refresh();
            }
        });

        chipAll.setOnClickListener(v -> setTypeFilter(TYPE_ALL));
        chipImages.setOnClickListener(v -> setTypeFilter(TYPE_IMAGES));
        chipText.setOnClickListener(v -> setTypeFilter(TYPE_TEXT));

        adapter = new AttachmentAdapter(new AttachmentAdapter.RowListener() {
            @Override
            public void onPreview(AttachmentRow row) {
                CatalogItem item = AppData.findCatalogItem(row.getCatalogId());
                DialogHelper.showPreview(requireContext(), item);
            }

            @Override
            public void onRemove(AttachmentRow row) {
                // remove icon: same confirm dialog, nothing to restore
                DialogHelper.showRemoveDialog(requireContext(), row.getName(), () -> removeLink(row.getLinkId()));
            }
        });
        recyclerFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFiles.setAdapter(adapter);

        // swipe right to remove
        new ItemTouchHelper(new FileSwipeCallback()).attachToRecyclerView(recyclerFiles);

        btnAddAttachment.setOnClickListener(v -> showAddAttachmentDialog());
        btnAddAttachmentEmpty.setOnClickListener(v -> showAddAttachmentDialog());
        btnResetEmpty.setOnClickListener(v -> resetSearch());

        syncing = true;
        edtSearch.setText(query);
        syncing = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private Project getProject() {
        return AppData.findProjectById(projectId);
    }

    private void setTypeFilter(int type) {
        typeFilter = type;
        refresh();
    }

    private void resetSearch() {
        query = "";
        typeFilter = TYPE_ALL;
        syncing = true;
        edtSearch.setText("");
        syncing = false;
        refresh();
    }

    // links stay in the order they were added. search is on the display name only
    private void refresh() {
        Project project = getProject();
        if (project == null) return;

        String q = query.trim().toLowerCase();

        ArrayList<AttachmentRow> rows = new ArrayList<>();
        for (AttachmentLink link : project.getAttachments()) {
            CatalogItem item = AppData.findCatalogItem(link.getCatalogId());
            if (item == null) continue;

            if (!q.isEmpty() && !item.getDisplayName().toLowerCase().contains(q)) continue;

            if (typeFilter == TYPE_IMAGES && item.getType() != CatalogItem.Type.IMAGE) continue;
            if (typeFilter == TYPE_TEXT && item.getType() != CatalogItem.Type.TEXT) continue;

            rows.add(new AttachmentRow(link, item));
        }
        adapter.submitList(rows);

        // chip counts always use the whole project, not the search result
        int total = project.getAttachments().size();
        chipAll.setText("All (" + total + ")");
        chipImages.setText("Images (" + project.getImageCount() + ")");
        chipText.setText("Text (" + project.getTextCount() + ")");
        showChipSelected(chipAll, typeFilter == TYPE_ALL);
        showChipSelected(chipImages, typeFilter == TYPE_IMAGES);
        showChipSelected(chipText, typeFilter == TYPE_TEXT);

        txtShowing.setText("Showing " + rows.size() + " of " + total + " attachments");

        if (total == 0) {
            layoutNoFiles.setVisibility(View.VISIBLE);
            layoutNoMatch.setVisibility(View.GONE);
            recyclerFiles.setVisibility(View.GONE);
        } else if (rows.size() == 0) {
            layoutNoFiles.setVisibility(View.GONE);
            layoutNoMatch.setVisibility(View.VISIBLE);
            recyclerFiles.setVisibility(View.GONE);
        } else {
            layoutNoFiles.setVisibility(View.GONE);
            layoutNoMatch.setVisibility(View.GONE);
            recyclerFiles.setVisibility(View.VISIBLE);
        }
    }

    private void showChipSelected(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(getResources().getColor(R.color.primary));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    // only items that are not linked to this project yet can be picked
    private void showAddAttachmentDialog() {
        Project project = getProject();

        ArrayList<CatalogItem> eligible = new ArrayList<>();
        for (CatalogItem item : AppData.getCatalog()) {
            if (!project.hasCatalogItem(item.getId())) {
                eligible.add(item);
            }
        }

        DialogHelper.showCatalogPicker(requireContext(), eligible, item -> {
            project.linkCatalogItem(item);
            host.onProjectChanged();
            refresh();
        });
    }

    // the link is found by its id, never by the row position
    private void removeLink(int linkId) {
        Project project = getProject();
        AttachmentLink link = project.findAttachmentById(linkId);
        if (link != null) {
            project.removeAttachment(link);
            host.onProjectChanged();
            refresh();
        }
    }

    // swipe a row to the right: ask for the attachment name. cancel or a wrong name puts the row back
    private class FileSwipeCallback extends ItemTouchHelper.SimpleCallback {

        private Paint paint = new Paint();

        FileSwipeCallback() {
            super(0, ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            AttachmentRow row = adapter.getCurrentList().get(position);
            DialogHelper.showRemoveDialog(requireContext(), row.getName(),
                    () -> removeLink(row.getLinkId()),
                    () -> adapter.notifyItemChanged(position));
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) {
                View item = viewHolder.itemView;
                Paint bg = new Paint();
                bg.setColor(Color.parseColor("#E5484D"));
                c.drawRect(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom(), bg);

                paint.setColor(Color.WHITE);
                paint.setTextSize(36f);
                paint.setFakeBoldText(true);
                c.drawText("Remove", item.getLeft() + 40f, item.getTop() + item.getHeight() / 2f + 12f, paint);
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
