package com.example.android.newsreader;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryUtils {

    //Log tag contains class name
    private static final String LOG_TAG = QueryUtils.class.getName();

    //Constructor is made private because no instances should be created of this class
    private QueryUtils() {
    }

    /*
    Main fetch method that will take the URL string and return the
    ArrayList to the loadInBackground method in the NewsLoader
    */
    public static List<News> fetchNewsItemsData(String url) {

        URL ncUrl = createUrl(url); //Build the URL from the passed string

        /*
         jsonResponse used to store the JSON response as string and use
         it later to extract JSON objects and arrays
        */
        String jsonResponse;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Initiate data fetch from the internet and store the resultant JSON string
        jsonResponse = initHttpRequest(ncUrl);

        /*
        Use the jsonResponse string to extract NewsItem properties and return
        ArrayList to the List<NewsItem> newsItems array
        */

        List<News> news = extractResultsFromJSON(jsonResponse);

        //Return the populated list the loadInBackground method in NewsLoader
        return news;
    }

    /*
    Method to extract JSON Objects and arrays and use them to
    get NewsItem data then add them to ArrayList and return it.
    */
    private static List<News> extractResultsFromJSON(String newsJSON) {

        //Check if the passed JSON string is empty, if so then just return null.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        //List used to store the data extracted from JSON objects and arrays
        List<News> newsItemsArrayList = new ArrayList<>();

        String title; //Store title
        String section; //Store section
        String url; //Store url of the news item
        String firstName; //Store the first name of contributor
        String lastName; //Store the last name of contributor
        String author; //The author consists of first name and last name (if any available
        String rawDate; //Store the raw date string from JSON object
        String date; //Stores the formatted raw date in NewsItem object

        /*
        Fetch JSON objects and arrays and use them to get NewsItem data
        and them to newsItemsArrayList
        */
        try {
            JSONObject rootJsonObject = new JSONObject(newsJSON);
            JSONObject responsesJasonObj = rootJsonObject.getJSONObject("response");
            JSONArray jsonResultsArray = responsesJasonObj.getJSONArray("results");

            /*
            Loop through the JSONArray and extract NewsItem data and
            then add them to newsItemsArrayList
            */
            for (int i = 0; i < jsonResultsArray.length(); i++) {
                JSONObject currentJson = jsonResultsArray.getJSONObject(i);
                title = currentJson.getString("webTitle");
                section = currentJson.getString("sectionName");
                url = currentJson.getString("webUrl");
                JSONArray tagsArray = currentJson.getJSONArray("tags");

                //Check if there is a tags JSONArray that contains contributor/author name
                if (!tagsArray.isNull(0)) {
                    JSONObject currentTagObj = tagsArray.getJSONObject(0);

                    //Check if there is first name and store otherwise set it to null
                    if (!currentTagObj.isNull("firstName")) {
                        firstName = currentTagObj.getString("firstName");
                    } else {
                        firstName = null;
                    }

                    //Check if there is last name and store otherwise set it to null
                    if (!currentTagObj.isNull("lastName")) {
                        lastName = currentTagObj.getString("lastName");
                    } else {
                        lastName = null;
                    }

                    //Call method to store formatted Author name
                    author = getAuthorName(firstName, lastName);
                } else {
                    author = null;
                }

                //Check if there is JSON date in the Json array otherwise return null
                if (!currentJson.isNull("webPublicationDate")) {
                    rawDate = currentJson.getString("webPublicationDate");
                    date = getFormattedDate(rawDate); //Format raw date and store it in date
                } else {
                    date = null;
                }

                //Add the fetched NewsItem properties to the newsItemsArrayList
                newsItemsArrayList.add(new News(title, section, author, date, url));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //return the newsItemsArrayList
        return newsItemsArrayList;

    }

    //Format the raw date fetched from the JSON object and a user friendly date
    private static String getFormattedDate(String rawDate) {
        if (rawDate == null) {
            return null;
        }
        Date date = null;
        SimpleDateFormat formattedDate = new SimpleDateFormat("MMM dd, yyyy - HH:mm");
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(rawDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate.format(date);
    }

    /*
    Get the first name and last name if available and return the
    formatted available names, otherwise null
    */
    private static String getAuthorName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        } else if (firstName == null || firstName.isEmpty()) {
            return lastName;
        } else if (lastName == null || lastName.isEmpty()) {
            return firstName;
        } else {
            return (firstName + " " + lastName);
        }
    }

    /*
    Initiate internet connection and fetch JSON string
    from the Json data source URL
    */
    private static String initHttpRequest(URL ncUrl) {
        String jsonResponse = null;

        if (ncUrl == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) ncUrl.openConnection();
            urlConnection.setConnectTimeout(1500);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonResponse;
    }

    //Read the raw byte stream and return the whole stream as String
    private static String readFromStream(InputStream inputStream) throws IOException {

        //Check if InputStream is available otherwise return null
        if (inputStream == null) {
            return null;
        }

        StringBuilder output = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            output.append(line);
            line = bufferedReader.readLine();
        }

        return output.toString();
    }

    //Method constructs URL from string and returns URL object
    private static URL createUrl(String url) {

        URL nUrl = null;
        //Checks if an empty url string has been passed
        if (url == null) {
            return nUrl;
        }

        //Try to construct URL from string and if failed then catch error and return null
        try {
            nUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "ERROR CREATING URL");
            e.printStackTrace();
        }

        return nUrl;
    }
}
