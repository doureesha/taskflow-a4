package com.example.taskflow;

import java.time.LocalDate;
import java.util.ArrayList;

// static store like assignment 2. holds the project list and the local attachment catalog
public class AppData {

    private static ArrayList<Project> projectList = new ArrayList<>();
    private static ArrayList<CatalogItem> catalog = new ArrayList<>();
    private static boolean loaded = false;

    public static void init() {
        if (loaded) return;
        loaded = true;
        loadCatalog();
        loadProjects();
    }

    public static ArrayList<Project> getProjectList() {
        return projectList;
    }

    public static ArrayList<CatalogItem> getCatalog() {
        return catalog;
    }

    public static void addProject(Project project) {
        projectList.add(project);
    }

    // always look up by unique id, never by name or row position
    public static Project findProjectById(int id) {
        for (Project p : projectList) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public static CatalogItem findCatalogItem(int id) {
        for (CatalogItem item : catalog) {
            if (item.getId() == id) return item;
        }
        return null;
    }

    private static void loadCatalog() {
        catalog.add(new CatalogItem(1, "sports-project.png", CatalogItem.Type.IMAGE, R.drawable.img_sports, null, "2.4 MB", "drawable/img_sports"));
        catalog.add(new CatalogItem(2, "client-brief.txt", CatalogItem.Type.TEXT, 0,
                "Client brief - Sports App\n\nGoals\n- Show the company services on one clean page\n- Use blue and red as the main colors\n- Keep the layout simple on small screens\n\nDeadline\n- First draft in two weeks\n- Final delivery after client review",
                "4 KB", "text/client_brief"));
        catalog.add(new CatalogItem(3, "mount-asset.png", CatalogItem.Type.IMAGE, R.drawable.img_mount, null, "1.1 MB", "drawable/img_mount"));
        catalog.add(new CatalogItem(4, "wireframe.jpg", CatalogItem.Type.IMAGE, R.drawable.img_wire, null, "860 KB", "drawable/img_wire"));
        catalog.add(new CatalogItem(5, "meeting-notes.txt", CatalogItem.Type.TEXT, 0,
                "Meeting notes\n\n1. Header needs a sticky menu\n2. Pricing table must be responsive\n3. Contact form needs validation\n4. Next review on Friday",
                "2 KB", "text/meeting_notes"));
        catalog.add(new CatalogItem(6, "logo-pack.png", CatalogItem.Type.IMAGE, R.drawable.img_logo, null, "3.0 MB", "drawable/img_logo"));
    }

    private static void loadProjects() {
        LocalDate today = LocalDate.now();

        Project p1 = new Project("User Interface", "Service Page Website",
                "Make a page display about services for company website in blue and red colors, include information on each product card.", 3);
        p1.addTask(new Task("Build header layout", Task.Priority.HIGH, Task.Status.TODO, today));
        p1.addTask(new Task("Add form validation", Task.Priority.HIGH, Task.Status.TODO, today));
        p1.addTask(new Task("Create brand guideline for website design", Task.Priority.NORMAL, Task.Status.DONE, today.minusDays(3)));
        p1.addTask(new Task("Create low fidelity service page website", Task.Priority.NORMAL, Task.Status.TODO, today.minusDays(5)));
        p1.addTask(new Task("Configure pricing table responsive grid", Task.Priority.NORMAL, Task.Status.IN_PROGRESS, today.plusDays(2)));
        p1.addTask(new Task("Draft hero copy and CTA text", Task.Priority.LOW, Task.Status.DONE, today.minusDays(4)));
        p1.addTask(new Task("Set up navigation menu", Task.Priority.NORMAL, Task.Status.TODO, today.minusDays(2)));
        p1.addTask(new Task("Design footer section", Task.Priority.LOW, Task.Status.TODO, today.plusDays(5)));
        p1.addTask(new Task("Write contact page copy", Task.Priority.NORMAL, Task.Status.IN_PROGRESS, null));
        p1.addTask(new Task("Optimize hero images", Task.Priority.HIGH, Task.Status.IN_PROGRESS, today.plusDays(1)));
        p1.addTask(new Task("Choose color palette", Task.Priority.NORMAL, Task.Status.DONE, today.minusDays(8)));
        p1.addTask(new Task("Collect client logos", Task.Priority.LOW, Task.Status.DONE, null));
        p1.linkCatalogItem(findCatalogItem(1));
        p1.linkCatalogItem(findCatalogItem(2));
        p1.linkCatalogItem(findCatalogItem(3));
        projectList.add(p1);

        Project p2 = new Project("Backend", "Login API",
                "Build authentication endpoints and JWT token refresh handlers", 2);
        fillTasks(p2, 10, 3);
        p2.linkCatalogItem(findCatalogItem(5));
        p2.linkCatalogItem(findCatalogItem(4));
        projectList.add(p2);

        Project p3 = new Project("Frontend", "Mobile Dashboard",
                "Responsive overview widgets and telemetry cards", 1);
        fillTasks(p3, 8, 2);
        p3.linkCatalogItem(findCatalogItem(6));
        projectList.add(p3);

        Project p4 = new Project("QA & Ops", "Release Checklist",
                "Pre-production verification and smoke tests", 2);
        fillTasks(p4, 4, 4);
        p4.linkCatalogItem(findCatalogItem(5));
        projectList.add(p4);

        Project p5 = new Project("Design", "Design Assets",
                "Vector icons and illustration library for landing pages", 0);
        projectList.add(p5);
    }

    // simple sample tasks: the first `done` tasks are Done, the rest are To do
    private static void fillTasks(Project project, int total, int done) {
        for (int i = 1; i <= total; i++) {
            Task.Status status = Task.Status.TODO;
            if (i <= done) status = Task.Status.DONE;
            project.addTask(new Task("Task " + i + " for " + project.getName(), Task.Priority.NORMAL, status, null));
        }
    }
}
