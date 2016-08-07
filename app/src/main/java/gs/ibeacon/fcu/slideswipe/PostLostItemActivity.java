package gs.ibeacon.fcu.slideswipe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import gs.ibeacon.fcu.slideswipe.JSON.JSON;

public class PostLostItemActivity extends AppCompatActivity {
    private EditText NameText;
    private EditText LocationText;
    private EditText TimeText;
    private EditText CostText;
    private EditText DescriptionText;
    private Button PostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_lost_item);
        PostButton = (Button) findViewById(R.id.postbutton);
        NameText = (EditText) findViewById(R.id.lostname);
        TimeText = (EditText) findViewById(R.id.losttime);
        CostText = (EditText) findViewById(R.id.cost);
        DescriptionText = (EditText) findViewById(R.id.description);

        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject LostItemJsonObject = new JSONObject();
                try {
                    LostItemJsonObject.put(JSON.KEY_STATE, JSON.STATE_POST_ITEM);
                    LostItemJsonObject.put(JSON.KEY_ITEM_NAME, NameText.getText());
                    LostItemJsonObject.put(JSON.KEY_LOST_TIME, TimeText.getText());
                    LostItemJsonObject.put(JSON.KEY_LOST_COST, CostText.getText());
                    LostItemJsonObject.put(JSON.KEY_LOST_DESCRIPTION, DescriptionText.getText());
                    ServerHandler.getInstance().sendToServer(LostItemJsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });


    }
}
