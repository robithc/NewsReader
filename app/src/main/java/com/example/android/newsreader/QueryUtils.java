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

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    public static List<News> fetchNewsData(String requestUrl) {

        URL url = createUrl(requestUrl);
        String jsonResponse = null;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsonResponse = makeHttpRequest(url);

        List<News> news = extractFeatureFromJSON(jsonResponse);
        return news;
    }

    private static List<News> extractFeatureFromJSON(String newsJSON) {

        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        List<News> newsItemsArrayList = new ArrayList<>();

        String title;
        String section;
        String url;
        String firstName;
        String lastName;
        String author;
        String rawDate;
        String formattedDate;

        try {
            JSONObject baseJsonObject = new JSONObject(newsJSON);
            JSONObject responseJsonObj = baseJsonObject.getJSONObject("response");
            JSONArray jsonResultsArray = responseJsonObj.getJSONArray("results");

            for (int i = 0; i < jsonResultsArray.length(); i++) {
                JSONObject currentNews = jsonResultsArray.getJSONObject(i);
                title = currentNews.getString("webTitle");
                section = currentNews.getString("sectionName");
                url = currentNews.getString("webUrl");
                JSONArray tagsArray = currentNews.getJSONArray("tags");

                if (!tagsArray.isNull(0)) {
                    JSONObject currentTagObj = tagsArray.getJSONObject(0);

                    if (!currentTagObj.isNull("firstName")) {
                        firstName = currentTagObj.getString("firstName");
                    } else {
                        firstName = null;
                    }

                    if (!currentTagObj.isNull("lastName")) {
                        lastName = currentTagObj.getString("lastName");
                    } else {
                        lastName = null;
                    }

                    author = getAuthorName(firstName, lastName);
                } else {
                    author = null;
                }

                if (!currentNews.isNull("webPublicationDate")) {
                    rawDate = currentNews.getString("webPublicationDate");
                    formattedDate = getFormattedDate(rawDate);
                } else {
                    formattedDate = null;
                }

                News createdNews = new News(title, section, author, formattedDate, url);
                newsItemsArrayList.add(createdNews);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsItemsArrayList;
    }

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

    private static String makeHttpRequest(URL ncUrl) {
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

                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while (line != null) {
            output.append(line);
            line = reader.readLine();
        }

        return output.toString();
    }

    private static URL createUrl(String stringUrl) {

        URL url = null;

        if (stringUrl == null) {
            return url;
        }

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "ERROR CREATING URL");
            e.printStackTrace();
        }
        return url;
    }
}
