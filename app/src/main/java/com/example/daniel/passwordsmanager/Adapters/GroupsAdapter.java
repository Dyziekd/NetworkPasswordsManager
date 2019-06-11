package com.example.daniel.passwordsmanager.Adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.daniel.passwordsmanager.DataModels.Group;
import com.example.daniel.passwordsmanager.R;

import java.util.ArrayList;

public class GroupsAdapter extends ArrayAdapter<Group>
{
    private ArrayList<Group> groups;

    public GroupsAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Group> data)
    {
        super(context, resource, data);
        groups = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // create new row view
        View newRow = convertView;
        if(newRow == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newRow = layoutInflater.inflate(R.layout.groups_list_item, null);
        }

        // create list row elements
        Holder holder = new Holder();
        holder.name = (TextView)newRow.findViewById(R.id.groups_list__name);
        holder.securityLevel = (TextView)newRow.findViewById(R.id.groups_list__security_level);
        newRow.setTag(holder);

        // get row data
        Group rowData = getItem(position);

        // set row data
        holder.name.setText(rowData.getName());
        holder.securityLevel.setText("Security level: " + String.valueOf(rowData.getSecurityLevel()));

        // return new row
        return newRow;
    }

    // returns group security level by id
    public int getGroupSecurityLevel(int idGroup)
    {
        for(int i=0; i<groups.size(); i++)
            if(groups.get(i).getId() == idGroup)
                return groups.get(i).getSecurityLevel();

        return 0;
    }

    private static class Holder
    {
        private TextView name, securityLevel;
    }
}
