package com.example.sergio.madlab;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.example.sergio.madlab.*;

public class AsyncTaskQueryISBN extends AsyncTask <String,Object,JSONObject>
{
    private EditText authorEditText;
    private EditText titleEditText;
    private EditText publisherEditText;
    private EditText publishedYearEditText;

    //Costruttore
    //Viene chiamato nel main thread, Usa la text view associata nel main
    public AsyncTaskQueryISBN(EditText aet, EditText tet, EditText pet, EditText pyet) {
        authorEditText = aet;
        titleEditText = tet;
        publisherEditText = pet;
        publishedYearEditText = pyet;
    }

    @Override
    protected JSONObject doInBackground(String... isbns) {
        String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbns[0];
        try{
            HttpURLConnection connection = null;
            // Build Connection.
            try{
                URL url = new URL(apiUrlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(5000); // 5 seconds
                connection.setConnectTimeout(5000); // 5 seconds
            } catch (MalformedURLException | ProtocolException e) {
                // Impossible: The only two URLs used in the app are taken from string resources.
                e.printStackTrace();
            }
            int responseCode = connection.getResponseCode();
            if(responseCode != 200){
                Log.w(getClass().getName(), "GoogleBooksAPI request failed. Response Code: " + responseCode);
                connection.disconnect();
                return null;
            }

            // Read data from response.
            StringBuilder builder = new StringBuilder();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = responseReader.readLine();
            while (line != null){
                builder.append(line);
                line = responseReader.readLine();
            }
            String responseString = builder.toString();
            Log.d(getClass().getName(), "Response String: " + responseString);
            JSONObject responseJson = new JSONObject(responseString);
            // Close connection and return response code.
            connection.disconnect();
            return responseJson;
        } catch (SocketTimeoutException e) {
            Log.w(getClass().getName(), "Connection timed out. Returning null");
            return null;
        } catch(IOException e){
            Log.d(getClass().getName(), "IOException when connecting to Google Books API.");
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            Log.d(getClass().getName(), "JSONException when connecting to Google Books API.");
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onPostExecute(JSONObject responseObject)
    {
        JSONArray items=null;
        JSONObject firstitem=null;
        JSONObject volumeInfo=null;
        String title;
        JSONArray authors;
        String author;
        String publisher;
        String publishedDate;

        if(responseObject == null){
            titleEditText.setText(R.string.no_data);
            authorEditText.setText(R.string.no_data);
            publisherEditText.setText(R.string.no_data);
            publishedYearEditText.setText(R.string.no_data);
            return;
        }

        try {
            items = responseObject.getJSONArray("items");
        }
        catch (JSONException e) {e.printStackTrace();}

        try {
            if (items == null || items.getJSONObject(0) == null) {
                titleEditText.setText(R.string.no_data);
                authorEditText.setText(R.string.no_data);
                publisherEditText.setText(R.string.no_data);
                publishedYearEditText.setText(R.string.no_data);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("tag"," item is "+items.toString());

        try {
            firstitem = items.getJSONObject(0);
        }
        catch (JSONException e) {e.printStackTrace();}
        Log.d("tag"," firstitem is "+firstitem);


        try {
            volumeInfo=firstitem.getJSONObject("volumeInfo");
        }
        catch (JSONException e) {e.printStackTrace();}
        Log.d("tag"," volumeinfo is "+volumeInfo);


        try {
            title = volumeInfo.getString("title");
        }
        catch (JSONException e) {e.printStackTrace(); title="Not Found";}
        Log.d("tag"," title is "+title);

        try {
            authors = volumeInfo.getJSONArray("authors");
            author = authors.getString(0);
            Log.d("tag", " author is " + author);
        }
        catch (JSONException e) {e.printStackTrace(); author="Not Found";}

        try {
            publishedDate = volumeInfo.getString("publishedDate");
        }
        catch (JSONException e) {e.printStackTrace(); publishedDate= "Not Found";}
        Log.d("tag"," publishedDate is "+publishedDate);

        try {
            publisher = volumeInfo.getString("publisher");
        }
        catch (JSONException e) {e.printStackTrace(); publisher="Not Found";}
        Log.d("tag"," publisher is "+publisher);

        titleEditText.setText(title);
        authorEditText.setText(author);
        publisherEditText.setText(publisher);
        publishedYearEditText.setText(publishedDate);

    }

}

