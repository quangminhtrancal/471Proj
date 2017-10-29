package com.example.leapa.connecttodatabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

public class MainActivity extends AppCompatActivity {
    boolean debugging = false;
    private String
            user = "DB_A2B7B9_471proj_admin",
            pass = "Robo1234",
            server = "sql7001.smarterasp.net", //If port number provided, the format will be "ipaddress:portnumber"
            db = "DB_A2B7B9_471proj",
            dbURL = "jdbc:jtds:sqlserver://" + server + ";" + "databseName=" + db + ";";

    TextView txtscan1;
    TextView txtscan2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        setContentView(R.layout.activity_main);

        // GUI element references
        final Button bu = (Button) findViewById(R.id.ConnectButton);
        final TextView t1 = (TextView) findViewById(R.id.TestDisplay);
        final Button clear = (Button) findViewById(R.id.ClearButton);

        txtscan1 = (TextView) findViewById(R.id.txtscan1);
        txtscan2 = (TextView) findViewById(R.id.txtscan2);

        //Bug fixes and magic
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        }catch(ClassNotFoundException e){ e.printStackTrace(); }

        //Listener: OnClick
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                t1.setText("");
            }
        });
        bu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try (Connection conn = DriverManager.getConnection(dbURL, user, pass)){
                    if (conn == null) {
                        addToDisplay(t1, "Not connected to DB");
                        return;
                    }
                    addToDisplay(t1, "Connecting to DB...");
//                    ArrayList<String> dbArr = new ArrayList<String>();
                    try(Statement stmt = conn.createStatement()) {
                        String selectQuery = "SELECT Name, Age FROM " + db + ".dbo.Data;";
                        ResultSet rs = stmt.executeQuery(selectQuery);
                        String s1;
                        while (rs.next()) {
                            s1 = "Name: " + rs.getString("Name")+"\t Age: "+Integer.toString(rs.getInt("Age"));
                            addToDisplay(t1,s1);
                        }
                        addToDisplay(t1, "Finished query");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // SCAN barcode
        Button scanbtn = (Button) findViewById(R.id.scan);
        final IntentIntegrator intentIntegrator=new IntentIntegrator(this);
        scanbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                intentIntegrator.initiateScan();
            }
        });
    }
 //   public void onButtonClickConnect(View v) throws SQLException { }


//=================DB convenience methods START===================================
    /*
    INACTIVE - Method to handle DB connections.
     */
//    @SuppressLint("NewApi")
    public Connection initConnection (String user, String pass, String db, String server) throws Exception {
        String connectionURL = "jdbc:jtds:sqlserver://" + server + ";" + "databseName=" + db + ";";
        return DriverManager.getConnection(connectionURL, user, pass);
    }

    /*
    INCOMPLETE - Method to handle insertions.
     */
    private void insertQuery(Statement stmt, String table, String[] values) throws SQLException {
        String insertQuery = "INSERT INTO " + db + ".dbo.Data VALUES (" + values + ");";
                        stmt.executeUpdate(insertQuery);
    }
//=================DB convenience methods END===================================

//=================GUI Covenience methods START===================================
    private void addToDisplay(TextView t1, String msg){
        msg = (t1.getText() == "") ?  msg : "\n" + msg; //Prepend new line if it's not the first

        t1.append(msg);
        if (debugging) Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
//=================GUI Covenience methods END===================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                //Picasso.with(this).load(result.getContents()).into(imgscan);
                try {
                    JSONObject jsonObject=new JSONObject(result.getContents());
                    txtscan2.setText("Name= "+jsonObject.getString("Name"));
                    txtscan1.setText("Address= "+jsonObject.getString("Address"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
