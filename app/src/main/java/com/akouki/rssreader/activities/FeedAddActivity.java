package com.akouki.rssreader.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.akouki.rssreader.R;
import com.akouki.rssreader.tasks.FeedAddTask;
import com.akouki.rssreader.tasks.FeedSearchTask;
import com.akouki.rssreader.utils.Utils;

public class FeedAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_add);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText editText = (EditText) findViewById(R.id.etUrl);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    FeedAddTask feedAddTask = new FeedAddTask(FeedAddActivity.this, editText.getText().toString());
                    feedAddTask.execute();
                } else {
                    Toast.makeText(FeedAddActivity.this, "There is no Internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    FeedSearchTask feedSearchTask = new FeedSearchTask(FeedAddActivity.this, editText.getText().toString());
                    feedSearchTask.execute();
                } else {
                    Toast.makeText(FeedAddActivity.this, "There is no Internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
