package com.example.campuscoret;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LiveMonitoringActivity extends AppCompatActivity {
    private static final long REFRESH_INTERVAL_MILLIS = 5_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshDashboard();
            handler.postDelayed(this, REFRESH_INTERVAL_MILLIS);
        }
    };

    private StudentActivityAdapter activityAdapter;
    private StudentLiveStatusAdapter statusAdapter;
    private TextView sessionsEmptyState;
    private TextView examsEmptyState;
    private LinearLayout sessionContainer;
    private LinearLayout examContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_monitoring);

        sessionContainer = findViewById(R.id.monitor_session_container);
        examContainer = findViewById(R.id.monitor_exam_container);
        sessionsEmptyState = findViewById(R.id.monitor_sessions_empty_state);
        examsEmptyState = findViewById(R.id.monitor_exams_empty_state);

        RecyclerView activityRecycler = findViewById(R.id.monitor_activity_recycler);
        activityAdapter = new StudentActivityAdapter();
        activityRecycler.setLayoutManager(new LinearLayoutManager(this));
        activityRecycler.setAdapter(activityAdapter);

        RecyclerView statusRecycler = findViewById(R.id.monitor_status_recycler);
        statusAdapter = new StudentLiveStatusAdapter();
        statusRecycler.setLayoutManager(new LinearLayoutManager(this));
        statusRecycler.setAdapter(statusAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshDashboard();
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MILLIS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshRunnable);
    }

    private void refreshDashboard() {
        int onlineCount = LiveMonitoringRepository.getOnlineStudentCount();
        List<StudentLiveStatus> statuses = LiveMonitoringRepository.getStudentStatuses();
        List<StudentActivityEvent> events = LiveMonitoringRepository.getRecentEvents();
        List<LiveSessionSummary> sessions = LiveMonitoringRepository.getActiveSessionSummaries();
        List<LiveExamSummary> exams = LiveMonitoringRepository.getLiveExamSummaries();

        ((TextView) findViewById(R.id.monitor_online_count)).setText(
                getString(R.string.monitor_online_count, onlineCount)
        );
        ((TextView) findViewById(R.id.monitor_active_sessions_count)).setText(
                getString(R.string.monitor_active_sessions_count, sessions.size())
        );
        ((TextView) findViewById(R.id.monitor_active_exams_count)).setText(
                getString(R.string.monitor_active_exams_count, exams.size())
        );

        activityAdapter.submitList(events);
        statusAdapter.submitList(statuses);
        bindSessions(sessions);
        bindExams(exams);
    }

    private void bindSessions(List<LiveSessionSummary> sessions) {
        sessionContainer.removeAllViews();
        if (sessions.isEmpty()) {
            sessionsEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        sessionsEmptyState.setVisibility(View.GONE);
        for (LiveSessionSummary session : sessions) {
            View view = getLayoutInflater().inflate(R.layout.item_monitor_session, sessionContainer, false);
            ((TextView) view.findViewById(R.id.monitor_session_title)).setText(
                    session.getSubjectName() + " - " + session.getClassName()
            );
            ((TextView) view.findViewById(R.id.monitor_session_meta)).setText(
                    getString(
                            R.string.monitor_session_meta,
                            session.getTeacherEmail(),
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                    .format(new Date(session.getStartedAtMillis()))
                    )
            );
            ((TextView) view.findViewById(R.id.monitor_session_attendance)).setText(
                    getString(
                            R.string.monitor_session_attendance,
                            session.getPresentCount(),
                            session.getTotalStudents(),
                            session.getAttendancePercentage()
                    )
            );
            sessionContainer.addView(view);
        }
    }

    private void bindExams(List<LiveExamSummary> exams) {
        examContainer.removeAllViews();
        if (exams.isEmpty()) {
            examsEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        examsEmptyState.setVisibility(View.GONE);
        for (LiveExamSummary exam : exams) {
            View view = getLayoutInflater().inflate(R.layout.item_monitor_exam, examContainer, false);
            ((TextView) view.findViewById(R.id.monitor_exam_title)).setText(
                    exam.getTitle() + " - " + exam.getClassName()
            );
            ((TextView) view.findViewById(R.id.monitor_exam_meta)).setText(exam.getSubjectName());
            ((TextView) view.findViewById(R.id.monitor_exam_participation)).setText(
                    getString(
                            R.string.monitor_exam_participation,
                            exam.getAttemptingCount(),
                            exam.getSubmittedCount(),
                            exam.getPendingCount()
                    )
            );
            examContainer.addView(view);
        }
    }
}
