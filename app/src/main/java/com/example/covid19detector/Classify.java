package com.example.covid19detector;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Classify extends AppCompatActivity {

    public Connection con;
    private String name, age, gen, diabetic, heart, anyOther;
    boolean profile;

    // presets for rgb conversion
    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    // options for model interpreter
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    // tflite graph
    private Interpreter tflite;
    // holds all the possible labels for model
    private List<String> labelList;
    // holds the selected image data as bytes
    private ByteBuffer imgData = null;
    // holds the probabilities of each label for non-quantized graphs
    private float[][] outputVal = null;
    // holds the probabilities of each label for quantized graphs
    private byte[][] labelProbArrayB = null;
    // array that holds the labels with the highest probabilities
    private String[] topLables = null;
    // array that holds the highest probabilities
    private String[] topConfidence = null;


    // selected classifier information received from extras
    private String chosen;
    private String result;


    // input image dimensions for the Inception Model
    private int DIM_IMG_SIZE_X = 224;
    private int DIM_IMG_SIZE_Y = 224;
    private int DIM_PIXEL_SIZE = 3;

    // int array to hold image data
    private int[] intValues;

    // activity elements
    private ImageView selected_image;
    private Button classify_button;
    private Button back_button;
    private TextView label1;
    private TextView label2;

    private TextView Confidence1;
    private TextView Confidence2;
    private ProgressBar progressBar2;


    // priority queue that will hold the top results from the CNN
    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get all selected classifier data from classifiers
        chosen = "model_unquant.tflite";


        // initialize array that holds image data
        intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        super.onCreate(savedInstanceState);

        //initialize graph and labels
        try{
            tflite = new Interpreter(loadModelFile());
            labelList = loadLabelList();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        // initialize byte array. The size depends if the input data needs to be quantized or not

        imgData =
                ByteBuffer.allocateDirect(
                        4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);

        imgData.order(ByteOrder.nativeOrder());

        // initialize probabilities array. The data types that array holds depends if the input data needs to be quantized or not

        outputVal = new float[1][2];


        setContentView(R.layout.activity_classify);

        name = (String) getIntent().getStringExtra("name");
        age = (String) getIntent().getStringExtra("age");
        gen = (String) getIntent().getStringExtra("gen");
        diabetic = (String) getIntent().getStringExtra("diabetic");
        heart = (String) getIntent().getStringExtra("heart");
        anyOther = (String) getIntent().getStringExtra("anyOther");

        // labels that hold top three results of CNN
        label1 = (TextView) findViewById(R.id.label1);
        label2 = (TextView) findViewById(R.id.label2);

        // displays the probabilities of top labels
        Confidence1 = (TextView) findViewById(R.id.Confidence1);
        Confidence2 = (TextView) findViewById(R.id.Confidence2);

        progressBar2 = (ProgressBar)findViewById(R.id.progressBar2);

        // initialize imageView that displays selected image to the user
        selected_image = (ImageView) findViewById(R.id.selected_image);

        // initialize array to hold top labels
        topLables = new String[RESULTS_TO_SHOW];
        // initialize array to hold top probabilities
        topConfidence = new String[RESULTS_TO_SHOW];

        // allows user to go back to activity to select a different image
        back_button = (Button)findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Classify.this, MainActivity.class);
                startActivity(i);
            }
        });

        // classify current displayed image
        classify_button = (Button)findViewById(R.id.classify_image);
        classify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get current bitmap from imageView
                Bitmap bitmap_orig = ((BitmapDrawable)selected_image.getDrawable()).getBitmap();
                // resize the bitmap to the required input size to the CNN
                Bitmap bitmap = getResizedBitmap(bitmap_orig, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y);
                // convert bitmap to byte array
                convertBitmapToByteBuffer(bitmap);
                // pass byte data to the graph
                tflite.run(imgData, outputVal);

                float inferedValue1 = outputVal[0][0];
                float inferedValue2 = outputVal[0][1];
                String finalAns;
                label1.setText("1. Covid");
                label2.setText("2. Normal");

                Confidence1.setText(String.format("%.0f%%", inferedValue1 * 100));
                Confidence2.setText(String.format("%.0f%%", inferedValue2 * 100));
                if((inferedValue1*100)>50)
                {
                    result = "Covid";
                }

                else
                {
                    result = "Normal";
                }

                CheckLogin checkLogin = new CheckLogin();
                checkLogin.execute("");

                if(Integer.parseInt(age)>59 || diabetic.equals("Yes") || heart.equals("Yes"))
                {
                    profile = true;
                }
                //Toast.makeText(Classify.this,diabetic + heart+String.valueOf(profile),Toast.LENGTH_SHORT).show();


                if (profile && result.equals("Covid"))
                {
                    openDialog();
                }


            }
        });

        // get image from previous activity to show in the imageView
        Uri uri = (Uri)getIntent().getParcelableExtra("resID_uri");
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            selected_image.setImageBitmap(bitmap);
            // not sure why this happens, but without this the image appears on its side
            selected_image.setRotation(selected_image.getRotation() + 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // loads tflite grapg from file
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(chosen);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // converts bitmap to byte array which is passed in the tflite graph
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float

                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);


            }
        }
    }

    // loads the labels from the label txt file in assets into a string array
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    // print the top labels and respective confidences
    private void printTopKLabels() {
        // add all results to priority queue
        for (int i = 0; i < labelList.size(); ++i) {

            sortedLabels.add(new AbstractMap.SimpleEntry<>(labelList.get(i), outputVal[0][i]));

            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        // get top results from priority queue
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            topLables[i] = label.getKey();
            topConfidence[i] = String.format("%.0f%%",label.getValue()*100);
        }

        // set the corresponding textviews with the results
        label1.setText(topLables[0]);
        //label2.setText("2. "+ "Normal");

        Confidence1.setText(topConfidence[0]);
        //Confidence2.setText((100)-Integer.parseInt(topConfidence[0])+"%");

    }


    // resizes bitmap to given dimensions
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }


    public class CheckLogin extends AsyncTask<String, String, String>
    {
        String z = "";
        Boolean isSuccess = false;
        String name1 = "";

        protected void onPreExecute()
        {
            progressBar2.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(String r)
        {
            progressBar2.setVisibility(View.GONE);
            Toast.makeText(Classify.this,r,Toast.LENGTH_SHORT).show();
            if(isSuccess)
            {
                Toast.makeText(Classify.this,"Data successfully sent to server",Toast.LENGTH_SHORT).show();
            }
        }

        protected String doInBackground(String... params) {

            try {
                con = connectionclass();
                if(con == null)
                {
                    z = "Check Your Internet Access";

                }

                else
                {
                    String query = "Insert into medicalinfo " +
                            " (name,age,gen,diabetic,heart,anyOther,result) values "
                            + "('"
                            + name
                            + "','"
                            + age
                            + "','"
                            + gen
                            + "','"
                            + diabetic
                            + "','"
                            + heart
                            + "','"
                            + anyOther
                            + "','"
                            + result
                            + "')";

                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    if (rs.next())
                    {
                        name1 = rs.getString("name");
                        z = "Query Executed Successfully";
                    }

                    else
                    {
                        z = "Invalid Query";
                        isSuccess = false;
                    }

                }
            }
            catch (Exception ex)
            {
                isSuccess = false;
                z = ex.getMessage();
                Log.d("SQL Error", z);
            }

            return z;
        }
    }


    @SuppressLint("NewApi")
    public Connection connectionclass(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://patilpratik24p.database.windows.net:1433;DatabaseName=personsinfo;user=pratik@patilpratik24p;password=@!17428343Pp;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
            connection = DriverManager.getConnection(ConnectionURL);
        }
        catch (SQLException se)
        {
            Log.e("Error here 1: " , se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("Error here 2: ",e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("Error here 3: ", e.getMessage());
        }
        return  connection;

    }

    public void openDialog(){
        AlertDialog alertDialog1 = new AlertDialog.Builder(
                Classify.this).create();
        String titleText = "Health Emergency!";
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(titleText);
        ssBuilder.setSpan(
                foregroundColorSpan,
                0,
                titleText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        // Setting Dialog Title
        alertDialog1.setTitle(ssBuilder);

        // Setting Dialog Message
        alertDialog1.setMessage("Please Visit Your Nearest Hospital");


        // Setting OK Button
        alertDialog1.setButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog
                // closed
                Toast.makeText(getApplicationContext(),
                        "Wishing you a speedy recovery. Take Care.", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog1.show();
    }
}