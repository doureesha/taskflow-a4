package com.example.taskflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;

// all the alert dialogs in one place so Home, Overview, Tasks and Files can reuse them
public class DialogHelper {

    public interface ProjectListener {
        void onSave(String name, String tag, String description, CatalogItem attachment);
    }

    public interface TaskListener {
        void onSave(String description, Task.Priority priority, Task.Status status, LocalDate dueDate);
        void onDelete();
    }

    public interface PickListener {
        void onPick(CatalogItem item);
    }

    // new project (existing == null) or edit project (existing != null, prefilled)
    public static void showProjectDialog(Context context, Project existing, ProjectListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_project, null);

        EditText edtName = view.findViewById(R.id.edtProjectName);
        EditText edtTag = view.findViewById(R.id.edtProjectTag);
        EditText edtDescription = view.findViewById(R.id.edtProjectDescription);
        View layoutAttachment = view.findViewById(R.id.layoutAttachment);
        TextView txtAttachment = view.findViewById(R.id.txtSelectedAttachment);
        Button btnAttachment = view.findViewById(R.id.btnChooseAttachment);

        CatalogItem[] selected = new CatalogItem[1];

        if (existing != null) {
            edtName.setText(existing.getName());
            edtTag.setText(existing.getTag());
            edtDescription.setText(existing.getDescription());
            layoutAttachment.setVisibility(View.GONE);
        }

        btnAttachment.setOnClickListener(v -> {
            showCatalogPicker(context, AppData.getCatalog(), item -> {
                selected[0] = item;
                txtAttachment.setText(item.getDisplayName());
            });
        });

        String title = "New Project";
        String saveText = "Add";
        if (existing != null) {
            title = "Edit Project";
            saveText = "Save";
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(saveText, null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        // set the click listener after show() so the dialog stays open when input is invalid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String tag = edtTag.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            boolean ok = true;
            if (name.length() < 1 || name.length() > 60) {
                edtName.setError("Name must be 1 to 60 characters");
                ok = false;
            }
            if (tag.length() < 1 || tag.length() > 20) {
                edtTag.setError("Tag must be 1 to 20 characters");
                ok = false;
            }
            if (description.length() < 1 || description.length() > 500) {
                edtDescription.setError("Description must be 1 to 500 characters");
                ok = false;
            }
            if (!ok) return;

            listener.onSave(name, tag, description, selected[0]);
            dialog.dismiss();
        });
    }

    // add task (existing == null) or edit task (existing != null, with a delete button)
    public static void showTaskDialog(Context context, Task existing, TaskListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_task, null);

        EditText edtDescription = view.findViewById(R.id.edtTaskDescription);
        TextView txtCounter = view.findViewById(R.id.txtCounter);
        Spinner spnPriority = view.findViewById(R.id.spnTaskPriority);
        Spinner spnStatus = view.findViewById(R.id.spnTaskStatus);
        TextView txtDueDate = view.findViewById(R.id.txtDueDate);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        Button btnClearDate = view.findViewById(R.id.btnClearDate);

        String[] priorities = {"High", "Normal", "Low"};
        String[] statuses = {"To do", "In progress", "Done"};

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPriority.setAdapter(priorityAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(statusAdapter);

        // defaults: Normal priority, To do status, no due date
        spnPriority.setSelection(Task.Priority.NORMAL.ordinal());
        spnStatus.setSelection(Task.Status.TODO.ordinal());

        LocalDate[] dueDate = new LocalDate[1];

        if (existing != null) {
            edtDescription.setText(existing.getDescription());
            spnPriority.setSelection(existing.getPriority().ordinal());
            spnStatus.setSelection(existing.getStatus().ordinal());
            dueDate[0] = existing.getDueDate();
        }

        txtCounter.setText(edtDescription.getText().length() + "/500");
        edtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtCounter.setText(s.length() + "/500");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        showDueDate(txtDueDate, dueDate[0]);

        btnPickDate.setOnClickListener(v -> {
            LocalDate start = dueDate[0];
            if (start == null) start = LocalDate.now();
            // DatePickerDialog months start from 0, LocalDate months start from 1
            DatePickerDialog picker = new DatePickerDialog(context, (datePicker, year, month, day) -> {
                dueDate[0] = LocalDate.of(year, month + 1, day);
                showDueDate(txtDueDate, dueDate[0]);
            }, start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth());
            picker.show();
        });

        btnClearDate.setOnClickListener(v -> {
            dueDate[0] = null;
            showDueDate(txtDueDate, null);
        });

        String title = "Add New Task";
        String saveText = "Add Task";
        if (existing != null) {
            title = "Edit Task";
            saveText = "Save";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(saveText, null)
                .setNegativeButton("Cancel", null);
        if (existing != null) {
            builder.setNeutralButton("Delete", null);
        }
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String description = edtDescription.getText().toString().trim();
            if (description.length() < 1 || description.length() > 500) {
                edtDescription.setError("Description must be 1 to 500 characters");
                return;
            }
            Task.Priority priority = Task.Priority.values()[spnPriority.getSelectedItemPosition()];
            Task.Status status = Task.Status.values()[spnStatus.getSelectedItemPosition()];
            listener.onSave(description, priority, status, dueDate[0]);
            dialog.dismiss();
        });

        if (existing != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                listener.onDelete();
                dialog.dismiss();
            });
        }
    }

    private static void showDueDate(TextView txtDueDate, LocalDate date) {
        if (date == null) {
            txtDueDate.setText("No due date");
        } else {
            txtDueDate.setText(Task.formatDate(date));
        }
    }

    // single choice catalog dialog. Add stays disabled until one item is selected
    public static void showCatalogPicker(Context context, ArrayList<CatalogItem> items, PickListener listener) {
        if (items.size() == 0) {
            Toast.makeText(context, "Every catalog item is already attached", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            CatalogItem item = items.get(i);
            names[i] = item.getDisplayName() + "  (" + item.typeLabel() + ")";
        }

        int[] chosen = {-1};

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Choose attachment")
                .setSingleChoiceItems(names, -1, (d, which) -> {
                    chosen[0] = which;
                    ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                })
                .setPositiveButton("Add", (d, which) -> {
                    if (chosen[0] >= 0) listener.onPick(items.get(chosen[0]));
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    // shows the real image or the real text
    public static void showPreview(Context context, CatalogItem item) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_preview, null);
        ImageView imgPreview = view.findViewById(R.id.imgPreview);
        TextView txtPreview = view.findViewById(R.id.txtPreview);

        if (item.getType() == CatalogItem.Type.IMAGE) {
            imgPreview.setImageResource(item.getImageRes());
            imgPreview.setVisibility(View.VISIBLE);
            txtPreview.setVisibility(View.GONE);
        } else {
            txtPreview.setText(item.getTextContent());
            txtPreview.setVisibility(View.VISIBLE);
            imgPreview.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(context)
                .setTitle(item.getDisplayName())
                .setView(view)
                .setPositiveButton("Close", null)
                .show();
    }

    // remove icon: nothing has to be restored if the user backs out
    public static void showRemoveDialog(Context context, String attachmentName, Runnable onConfirm) {
        showRemoveDialog(context, attachmentName, onConfirm, () -> { });
    }

    // the user must type the attachment name before it is removed.
    // onCancel runs when the user cancels or types a wrong name, so a swiped row can be put back
    public static void showRemoveDialog(Context context, String attachmentName, Runnable onConfirm, Runnable onCancel) {
        EditText edtName = new EditText(context);
        edtName.setHint(attachmentName);
        edtName.setSingleLine(true);
        edtName.setPadding(48, 32, 48, 32);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Remove attachment")
                .setMessage("Type the attachment name to confirm:\n" + attachmentName)
                .setView(edtName)
                .setPositiveButton("Remove", null)
                .setNegativeButton("Cancel", (d, which) -> onCancel.run())
                .setOnCancelListener(d -> onCancel.run())
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String typed = edtName.getText().toString().trim();
            if (!typed.equals(attachmentName)) {
                // wrong name: the attachment stays linked
                Toast.makeText(context, "Name does not match, attachment kept", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                onCancel.run();
                return;
            }
            onConfirm.run();
            dialog.dismiss();
        });
    }

    // swipe right on a task: ask first. onCancel puts the swiped row back
    public static void showDeleteTaskDialog(Context context, String taskDescription, Runnable onConfirm, Runnable onCancel) {
        new AlertDialog.Builder(context)
                .setTitle("Delete task")
                .setMessage("Do you want to delete this task?\n\n" + taskDescription)
                .setPositiveButton("Delete", (d, which) -> onConfirm.run())
                .setNegativeButton("Cancel", (d, which) -> onCancel.run())
                .setOnCancelListener(d -> onCancel.run())
                .show();
    }
}
