package com.example.ticket_machine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ticket_machine.models.Event;
import com.example.ticket_machine.tools.JsonParser;
import com.example.ticket_machine.ui.scanner.ScannerEventsActivity;
import com.example.ticket_machine.ui.scanner.ScannerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    /**
     * The following class is based on Master-Detail Flow scheme.
     * It is used to generate events list with listeners, which will pass clicked event ID to the details view.
     */
    private boolean mTwoPane;
    private static String URL_GETEVENTS;
    private JsonParser jsonParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_list);

        jsonParser = new JsonParser();

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.scanner_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {

        URL_GETEVENTS = getString(R.string.URL_EVENTS);

        final List<Event> event_list = new ArrayList<>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GETEVENTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    Event event = JsonParser.getEvent(object);

                                    event_list.add(event);
                                }
                                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(ScannerActivity.this, event_list, mTwoPane));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ScannerActivity.this, "Cannot get events. Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ScannerActivity.this, "Cannot get events. Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ScannerActivity.this);
        requestQueue.add(stringRequest);
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ScannerActivity mParentActivity;
        private final List<Event> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = (Event) view.getTag();
                if (mTwoPane) {
                    /**
                     * Passing event ID, which was chosen from the list, as arguments bundle.
                     */
                    Bundle arguments = new Bundle();
                    arguments.putString(ScannerFragment.ARG_ITEM_ID, event.Id);
                    ScannerFragment fragment = new ScannerFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ScannerEventsActivity.class);
                    /**
                     * Passing event ID, which was chosen from the list, to intent.
                     */
                    intent.putExtra(ScannerFragment.ARG_ITEM_ID, event.Id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ScannerActivity parent,
                                      List<Event> event_list,
                                      boolean twoPane) {
            mValues = event_list;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scanner_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).Id);
            holder.mContentView.setText(mValues.get(position).Name);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}