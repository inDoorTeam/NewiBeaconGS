package gs.ibeacon.fcu.slideswipe;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import gs.ibeacon.fcu.slideswipe.JSON.JSON;

public class PostLostItemActivity extends AppCompatActivity {
    private EditText nameText;
    private EditText LocationText;
    private EditText timeText;
    private EditText costText;
    private EditText descriptionText;
    private Button postButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setIcon(R.mipmap.ic_launcher);
        getWindow().setStatusBarColor(getResources().getColor(R.color.toolbarU));
        actionBar.setTitle(Html.fromHtml("<font color='#00FFCC'>智慧導引</font>"));
        setContentView(R.layout.activity_post_lost_item);
        nameText = (EditText) findViewById(R.id.lostNameText);
        timeText = (EditText) findViewById(R.id.lostTimeText);
        costText = (EditText) findViewById(R.id.costText);
        descriptionText = (EditText) findViewById(R.id.descriptText);
        postButton = (Button) findViewById(R.id.postButton);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject LostItemJsonObject = new JSONObject();
                try {
                    LostItemJsonObject.put(JSON.KEY_STATE, JSON.STATE_POST_ITEM);
                    LostItemJsonObject.put(JSON.KEY_ITEM_NAME, nameText.getText());
                    LostItemJsonObject.put(JSON.KEY_LOST_TIME, timeText.getText());
                    LostItemJsonObject.put(JSON.KEY_LOST_COST, costText.getText());
                    LostItemJsonObject.put(JSON.KEY_LOST_DESCRIPTION, descriptionText.getText());
                    ServerHandler.getInstance().sendToServer(LostItemJsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(v.getContext(), "已傳送", Toast.LENGTH_LONG).show();

                finish();
            }

        });


    }
}
