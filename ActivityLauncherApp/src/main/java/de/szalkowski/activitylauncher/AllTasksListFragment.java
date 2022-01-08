package de.szalkowski.activitylauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.DynamicColors;

import org.thirdparty.LauncherIconCreator;

import java.util.Objects;

public class AllTasksListFragment extends Fragment implements AllTasksListAsyncProvider.Listener<AllTasksListAdapter>, Filterable {
    private RecyclerView reyclerViewTasks;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_list, container, false);
        reyclerViewTasks = (RecyclerView) view.findViewById(R.id.reyclerViewTasks);
        reyclerViewTasks.setLayoutManager(new LinearLayoutManager(view.getContext()));
        reyclerViewTasks.setHasFixedSize(true);
        // specify an adapter with the list to show
        /*
        this.reyclerViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    ExpandableListAdapter adapter = parent.getExpandableListAdapter();
                    MyActivityInfo info = (MyActivityInfo) adapter.getChild(groupPosition, childPosition);
                    var rooted = isRootAllowed();
                    LauncherIconCreator.launchActivity(getActivity(), info.component_name, rooted && info.is_private);
                    return false;

            }
        });
      */
        //this.reyclerViewTasks.setTextFilterEnabled(true);
        registerForContextMenu(this.reyclerViewTasks);
        AllTasksListAsyncProvider provider = new AllTasksListAsyncProvider(getActivity(), this);
        provider.execute();

        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
                                    ContextMenuInfo menuInfo) {
        var rooted = isRootAllowed();

        menu.add(Menu.NONE, 0, Menu.NONE, R.string.context_action_shortcut);
        if (rooted) {
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.context_action_shortcut_as_root);
        }
        menu.add(Menu.NONE, 2, Menu.NONE, R.string.context_action_launch);
        if (rooted) {
            menu.add(Menu.NONE, 3, Menu.NONE, R.string.context_action_launch_as_root);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }


    @Override
    public void onProviderFinished(AsyncProvider<AllTasksListAdapter> task, AllTasksListAdapter value) {

        try {
            this.reyclerViewTasks.setAdapter(value);
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.error_tasks, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Filter getFilter() {
        AllTasksListAdapter adapter = (AllTasksListAdapter) this.reyclerViewTasks.getAdapter();

        if (adapter != null) {
            return adapter.getFilter();
        } else {
            return null;
        }
    }

    private boolean isRootAllowed() {
        return ((MainActivity) Objects.requireNonNull(getActivity())).isRootAllowed();
    }
}
