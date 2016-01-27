package com.example.linusgranath.opencvtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

/**
 * Super class used to controll the global options menu
 * Created by Linus Granath on 2016-01-27.
 */
public class OptionsSuperClass extends Activity {

    private Button btnOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    /**
     * Create the menu if user presses options, menu located in res->menu->optionsmenu.xml
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);
        return true;
    }

    /**
     * Decides what action to take when one of the item menus are pressed
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        // Check for which item is selected
        switch(item.getItemId()){

            // Goto loop activity(screen)
            case R.id.menuItemLoop:
                gotoLoopActivity();
                break;

            // Goto object activity
            case R.id.menuItemObject:
                gotoObjectActivity();
                break;
        }
        return true;
    }

    /**
     * Chagnes to the loop activity TODO - finnish activity
     */
    public void gotoLoopActivity(){
        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);
    }

    /**
     * Changes to the object activity TODO - finnish activity
     */
    public void gotoObjectActivity(){
        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);
    }

}
