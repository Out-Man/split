package edu.berkeley.cs160.theccertservice.splist;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.NumberFormat;

import edu.berkeley.cs160.theccertservice.splist.PickUpActivity.ChooseListListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class CalcBillActivity extends ListActivity {
	

	private TextView MoneyOwed;
	private Button calcButton;
	private Spinner currentList;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayList<String> sharedLists;
    View header;
    View footer;
    ShoppingList prevList = null;
    
    String chosenList = "";
    public static ListActivity mainCalcBillAct = null;

    public void updateListNames() {
    	if (mainCalcBillAct != null) {
    		if (currentList != null && currentList.getSelectedItem() != null) {
    			prevList = ShoppingList.getSharedShoppingList(currentList.getSelectedItem().toString());
    		} else {
    			Log.d("Why","Now?");
    		}
    		sharedLists = ShoppingList.allSharedListNames();
    		ArrayList<String> sharedListsToCalc = new ArrayList<String>();
    		for (String l : sharedLists) {
    			boolean hasAnAcceptedItem = false;
    			ArrayList<Item> items = ShoppingList.getSharedShoppingList(l)._items;
    			for (Item i : items) {
    				if (i._shareAccepted) {
    					hasAnAcceptedItem = true;
    				}
    			}
    			if (hasAnAcceptedItem) {
    				sharedListsToCalc.add(l);
    			}
    		}
    		sharedLists = sharedListsToCalc;
    		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sharedLists);
    		currentList = (Spinner) header.findViewById(R.id.spinner1);
    		currentList.setAdapter(arrayAdapter);
    		currentList.setOnItemSelectedListener(new ChooseListListener());
    		if (prevList != null) {
    			currentList.setSelection(sharedLists.indexOf(prevList));
    		}
    	}
    }

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calcbill);
		mainCalcBillAct = this;
		
	    header = getLayoutInflater().inflate(R.layout.calcbill_header, null);
	    footer = getLayoutInflater().inflate(R.layout.calcbill_footer, null);
	    ListView listView = (ListView) findViewById(R.id.listView1);
	    listView.addHeaderView(header);
	    listView.addFooterView(footer);
    
	    adapter=new ArrayAdapter<String>(this,
	    		android.R.layout.simple_list_item_1, listItems);
	    listView.setAdapter(adapter);
		
		MoneyOwed = (TextView) header.findViewById(R.id.money_owed);
		
		updateListNames();
	}
	
	
	
	public String calculateOwed(ShoppingList lst) {
		double owed = 0.0;
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		listItems.clear();
		if (lst != null) {
			for (Item i : lst.getItems()) {
				if (i._shareAccepted) {
					String itemName = i.getName();
					double itemPrice = i.getPrice();
					double numPplSharing = i.getNumPeopleSharing() + 1.0;
					double priceFrac = roundTwoDecimalPlaces(itemPrice / numPplSharing);
					String priceFracStr = formatter.format(priceFrac);
					owed = owed + priceFrac;
					String nameAndPrice = itemName + " costs " + priceFracStr;
					listItems.add(nameAndPrice);
					adapter.notifyDataSetChanged();
				}
			}
		}
		String moneyString = formatter.format(owed);
		return moneyString;
	}
	
	public double roundTwoDecimalPlaces(double d) {
        DecimalFormat twoPlaces = new DecimalFormat("##.##");
        return Double.valueOf(twoPlaces.format(d));
	}

	public class ChooseListListener extends Activity implements OnItemSelectedListener {
		
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			chosenList = ((Spinner) parent).getSelectedItem().toString();
    		ShoppingList curList = ShoppingList.getSharedShoppingList(chosenList); // list of shared items for that shopping list
    		MoneyOwed = (TextView) header.findViewById(R.id.money_owed);
			MoneyOwed.setText(calculateOwed(curList));
			prevList = curList;
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}
}
