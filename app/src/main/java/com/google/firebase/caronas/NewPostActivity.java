package com.google.firebase.caronas;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.User;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;


    private EditText mSourceField;
    private EditText mDestinyField;
    private EditText mTimeField;
    private FloatingActionButton mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);


        mDatabase = FirebaseDatabase.getInstance().getReference();


        mSourceField = findViewById(R.id.field_source);
        mDestinyField = findViewById(R.id.field_destiny);
        mTimeField = findViewById(R.id.field_time);
        mSubmitButton = findViewById(R.id.fab_submit_post);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String source = mSourceField.getText().toString();
        final String destiny = mDestinyField.getText().toString();
        final String time = mTimeField.getText().toString();

        if (TextUtils.isEmpty(source)) {
            mSourceField.setError(REQUIRED);
            return;
        }


        if (TextUtils.isEmpty(destiny)) {
            mDestinyField.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(time)) {
            mTimeField.setError(REQUIRED);
            return;
        }


        setEditingEnabled(false);
        Toast.makeText(this, "Postando...", Toast.LENGTH_SHORT).show();


        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);


                        if (user == null) {

                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            writeNewPost(userId, user.username, source, destiny, time);
                        }

                        setEditingEnabled(true);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        mSourceField.setEnabled(enabled);
        mDestinyField.setEnabled(enabled);
        mTimeField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String username, String source, String destiny, String time) {
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, source, destiny, time);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
}