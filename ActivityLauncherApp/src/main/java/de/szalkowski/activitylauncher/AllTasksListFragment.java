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
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
        ExpandableListView list = requireView().findViewById(R.id.expandableListView1);

        switch (ExpandableListView.getPackedPositionType(info.packedPosition)) {
            case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
                MyActivityInfo activity = (MyActivityInfo) list.getExpandableListAdapter().getChild(ExpandableListView.getPackedPositionGroup(info.packedPosition), ExpandableListView.getPackedPositionChild(info.packedPosition));
                switch (item.getItemId()) {
                    case 0:
                        LauncherIconCreator.createLauncherIcon(getActivity(), activity);
                        break;
                    case 1:
                        RootLauncherIconCreator.createLauncherIcon(getActivity(), activity);
                        break;
                    case 2:
                        LauncherIconCreator.launchActivity(getActivity(), activity.component_name, false);
                        break;
                    case 3:
                        LauncherIconCreator.launchActivity(getActivity(), activity.component_name, true);
                        break;
                    case 4:
                        DialogFragment dialog = new ShortcutEditDialogFragment();
                        Bundle args = new Bundle();
                        args.putParcelable("activity", activity.component_name);
                        args.putBoolean("as_root", activity.is_private);
                        dialog.setArguments(args);
                        dialog.show(getChildFragmentManager(), "ShortcutEditor");
                        break;
                }
                break;

            case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
                MyPackageInfo pack = (MyPackageInfo) list.getExpandableListAdapter().getGroup(ExpandableListView.getPackedPositionGroup(info.packedPosition));
                switch (item.getItemId()) {
                    case 0:
                        LauncherIconCreator.createLauncherIcon(requireActivity(), pack);
                        Toast.makeText(getActivity(), getString(R.string.error_no_default_activity), Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        PackageManager pm = requireActivity().getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage(pack.package_name);
                        if (intent != null) {
                            Toast.makeText(getActivity(), String.format(getText(R.string.starting_application).toString(), pack.name), Toast.LENGTH_LONG).show();
                            requireActivity().startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error_no_default_activity), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
        }
        return super.onContextItemSelected(item);
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
