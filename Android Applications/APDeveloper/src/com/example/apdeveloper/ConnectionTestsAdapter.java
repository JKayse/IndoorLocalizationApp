package com.example.apdeveloper;



import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ConnectionTestsAdapter extends BaseExpandableListAdapter{
	LayoutInflater inflater;
	Context context;
	ArrayList<ConnectionTest> connections;

	
	//Class constructor that takes in the data and the context.
	ConnectionTestsAdapter(Context context, ArrayList<ConnectionTest> data){
		super();
		this.context = context;
		connections = data;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	//Gets the child at a position.
	public APStrength getChild(int groupPosition, int childPosition) {
        return connections.get(groupPosition).getAP(childPosition);
    }
	
	//Gets the child view and inflates it.
	public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        final APStrength currentStrength = getChild(groupPosition, childPosition);
         
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ap_item, null);
        }
        
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView strength = (TextView) convertView.findViewById(R.id.strength);
		
        name.setText(currentStrength.getName());
        strength.setText(currentStrength.getStrength());
        return convertView;
	}
	
	
	//Gets the categories and inflates them.
	public View getGroupView(int groupPosition, boolean isExpanded,
	            View convertView, ViewGroup parent) {
			ConnectionTest temp = getGroup(groupPosition);
	        if (convertView == null) {
	            LayoutInflater secondInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = secondInflater.inflate(R.layout.test_item,null);
	        }
	        TextView name = (TextView) convertView.findViewById(R.id.testName);
	        name.setText(temp.getTestName());

	        return convertView;
	    }

	//Returns the number of categories.
	@Override
	public int getGroupCount() {
		return connections.size();
	}

	//Returns the number of grades for a certain category.
	@Override
	public int getChildrenCount(int groupPosition) {
		return connections.get(groupPosition).getAPs().size();
	}

	//Returns the category at a certain position.
	@Override
	public ConnectionTest getGroup(int groupPosition) {
		return connections.get(groupPosition);
	}

	//Returns the id of the category.
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	//Returns the id of the grade.
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	//Makes the each id is stable...
	@Override
	public boolean hasStableIds() {
		return true;
	}

	
	//Says that you can select a child.
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
